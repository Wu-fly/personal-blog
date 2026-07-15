<template>
  <div class="article-detail-page">
    <div class="article-container">
      <!-- 文章主体 -->
      <div class="article-main">
        <!-- 文章头部 -->
        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <span class="author" @click="goToSpace(article.authorId)">
              <el-avatar :size="32" :src="article.authorAvatar" />
              {{ article.authorName }}
            </span>
            <span class="time">{{ article.createTime }}</span>
            <span class="views"><el-icon><View /></el-icon> {{ article.viewCount }}</span>
            <el-tag v-if="article.isPaid" type="warning" size="small">付费文章</el-tag>
          </div>
          <div class="article-tags">
            <el-tag v-for="tag in article.tags" :key="tag.id || tag" size="small" class="tag">
              {{ tag.name || tag }}
            </el-tag>
          </div>
        </div>

        <!-- 付费提示 -->
        <div v-if="article.isPaid && !article.hasPurchased" class="pay-notice">
          <el-icon><Lock /></el-icon>
          <span>本文为付费内容，需支付 ¥{{ article.price }} 解锁阅读</span>
          <el-button type="primary" @click="handlePurchase">立即购买</el-button>
        </div>

        <!-- 文章内容 -->
        <div class="article-content" v-html="processedContent"></div>

        <!-- 文章操作栏 -->
        <div class="article-actions">
          <el-button :type="article.isLiked ? 'primary' : 'default'" @click="handleLike">
            <el-icon><Star /></el-icon> {{ article.isLiked ? '已点赞' : '点赞' }} {{ article.likeCount }}
          </el-button>
          <el-button :type="article.isCollected ? 'warning' : 'default'" @click="handleCollect">
            <el-icon><Collection /></el-icon> {{ article.isCollected ? '已收藏' : '收藏' }} {{ article.collectCount }}
          </el-button>
          <el-button @click="handleReward">
            <el-icon><Present /></el-icon> 打赏
          </el-button>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-section">
        <h3 class="section-title">评论 ({{ commentTotal }})</h3>
        <!-- 评论输入框 -->
        <div class="comment-input">
          <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="写下你的评论..." />
          <el-button type="primary" @click="submitComment" :loading="submitting">发表评论</el-button>
        </div>
        <!-- 评论列表 -->
        <CommentList :comments="comments" @reply="handleReply" @delete="handleDeleteComment" />
      </div>
    </div>

    <!-- 侧边栏 -->
    <div class="article-sidebar">
      <!-- 作者信息卡片 -->
      <div class="author-card">
        <el-avatar :size="64" :src="article.authorAvatar" />
        <h4>{{ article.authorName }}</h4>
        <p class="author-bio">{{ article.authorBio }}</p>
        <div class="author-actions">
          <el-button 
            :type="isFollowingAuthor ? 'default' : 'primary'" 
            size="small" 
            @click="toggleFollowAuthor"
          >
            {{ isFollowingAuthor ? '已关注' : '关注' }}
          </el-button>
          <el-button type="default" size="small" @click="goToSpace(article.authorId)">访问空间</el-button>
        </div>
      </div>
    </div>

    <!-- 回到顶端 -->
    <BackToTop />

    <!-- 打赏弹窗 -->
    <el-dialog v-model="rewardDialogVisible" title="打赏作者" width="400px">
      <div class="reward-options">
        <div v-for="amount in rewardAmounts" :key="amount" :class="['reward-item', { active: rewardAmount === amount }]" @click="rewardAmount = amount">
          ¥{{ amount }}
        </div>
      </div>
      <el-input v-model.number="customAmount" placeholder="自定义金额" type="number" style="margin-top: 15px" />
      <template #footer>
        <el-button @click="rewardDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReward">确认打赏</el-button>
      </template>
    </el-dialog>

    <!-- 回复评论弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复评论" width="500px">
      <div class="reply-to-info">
        <span>回复给：</span>
        <span class="reply-to-user">{{ replyToComment?.nickname }}</span>
      </div>
      <div class="reply-original-comment">
        <p>{{ replyToComment?.content }}</p>
      </div>
      <el-input 
        v-model="replyContent" 
        type="textarea" 
        :rows="4" 
        placeholder="写下你的回复..." 
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="submittingReply">发表回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { View, Star, Collection, Present, Lock } from '@element-plus/icons-vue'
import CommentList from '@/components/CommentList.vue'
import BackToTop from '@/components/BackToTop.vue'
import { purchaseArticle, reward as rewardApi } from '@/api/wallet'
import { getItem, setItem } from '@/utils/storage'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const article = ref({
  id: '',
  title: '',
  content: '',
  authorId: '',
  authorName: '',
  authorAvatar: '',
  authorBio: '',
  createTime: '',
  viewCount: 0,
  likeCount: 0,
  collectCount: 0,
  isLiked: false,
  isCollected: false,
  isPaid: false,
  hasPurchased: false,
  price: 0,
  tags: []
})

const comments = ref([])
const commentTotal = ref(0)
const commentContent = ref('')
const submitting = ref(false)

// 打赏相关
const rewardDialogVisible = ref(false)
const rewardAmounts = [5, 10, 20, 50, 100]
const rewardAmount = ref(10)
const customAmount = ref(null)

// 回复评论相关
const replyDialogVisible = ref(false)
const replyToComment = ref(null)
const replyContent = ref('')
const submittingReply = ref(false)

// 关注作者相关
const isFollowingAuthor = ref(false)

// 将 Markdown 转换为 HTML
const convertMarkdownToHtml = (markdown) => {
  if (!markdown) return ''
  
  let html = markdown
  
  // 转义 HTML 特殊字符（但保留已有的 HTML 标签）
  const hasHtmlTags = /<[^>]+>/.test(html)
  if (!hasHtmlTags) {
    html = html
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
  }
  
  // 图片 - 必须在链接之前处理
  html = html.replace(/!\[(.*?)\]\((.*?)\)/g, '<img src="$2" alt="$1" />')
  
  // 链接
  html = html.replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // 标题
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>')
  
  // 粗体
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  
  // 斜体
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  // 代码块
  html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
  
  // 行内代码
  html = html.replace(/`(.*?)`/g, '<code>$1</code>')
  
  // 引用
  html = html.replace(/^&gt; (.*$)/gim, '<blockquote>$1</blockquote>')
  html = html.replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>')
  
  // 无序列表
  html = html.replace(/^\* (.*$)/gim, '<li>$1</li>')
  html = html.replace(/^- (.*$)/gim, '<li>$1</li>')
  
  // 包裹列表项
  html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
  
  // 段落 - 将连续的非标签行包裹在 <p> 中
  const lines = html.split('\n')
  let inParagraph = false
  let result = []
  
  for (let line of lines) {
    const trimmedLine = line.trim()
    
    // 如果是空行
    if (!trimmedLine) {
      if (inParagraph) {
        result.push('</p>')
        inParagraph = false
      }
      continue
    }
    
    // 如果是 HTML 标签开头
    if (trimmedLine.match(/^<(h[1-6]|ul|ol|li|blockquote|pre|code|img|div)/)) {
      if (inParagraph) {
        result.push('</p>')
        inParagraph = false
      }
      result.push(line)
    } else {
      // 普通文本
      if (!inParagraph) {
        result.push('<p>')
        inParagraph = true
      }
      result.push(line.replace(/\n/g, '<br>'))
    }
  }
  
  if (inParagraph) {
    result.push('</p>')
  }
  
  return result.join('\n')
}

// 计算属性：处理后的文章内容
const processedContent = computed(() => {
  if (!article.value || !article.value.content) return ''
  
  // 检查内容是否已经是 HTML 格式
  const content = article.value.content
  if (content.includes('<img') || content.includes('<h1') || content.includes('<h2')) {
    // 已经是 HTML 格式，直接返回
    return content
  }
  
  // 是 Markdown 格式，需要转换
  return convertMarkdownToHtml(content)
})

// 模拟文章数据
const mockArticles = {
  '1': {
    id: '1',
    title: '胡雪岩的商业智慧：从学徒到红顶商人',
    id: '1',
    title: '胡雪岩的商业智慧：从学徒到红顶商人',
    previewContent: `
      <h2 id="intro">引言</h2>
      <p>胡雪岩（1823-1885），清朝著名徽商，被誉为"红顶商人"。他从一个钱庄学徒起步，最终成为清朝首富，其传奇经历至今仍被人们津津乐道。</p>
      
      <h2 id="early">早年经历</h2>
      <p>胡雪岩出生于安徽绩溪，12岁便到杭州一家钱庄当学徒。他勤奋好学，善于观察，很快就掌握了钱庄的经营之道。</p>
      
      <div class="locked-content">
        <p>🔒 <em>以下为付费内容，购买后可查看完整文章...</em></p>
      </div>
    `,
    content: `
      <h2 id="intro">引言</h2>
      <p>胡雪岩（1823-1885），清朝著名徽商，被誉为"红顶商人"。他从一个钱庄学徒起步，最终成为清朝首富，其传奇经历至今仍被人们津津乐道。</p>
      
      <h2 id="early">早年经历</h2>
      <p>胡雪岩出生于安徽绩溪，12岁便到杭州一家钱庄当学徒。他勤奋好学，善于观察，很快就掌握了钱庄的经营之道。</p>
      
      <h2 id="business">经商之道</h2>
      <p>胡雪岩的成功离不开他独特的经商智慧：</p>
      <ul>
        <li><strong>诚信为本</strong>："做生意先做人，做人先讲诚信"</li>
        <li><strong>广结善缘</strong>：善于结交各方人士，建立广泛的人脉网络</li>
        <li><strong>审时度势</strong>：善于把握时机，在乱世中寻找商机</li>
        <li><strong>回馈社会</strong>：创办胡庆余堂，济世救人</li>
      </ul>
      
      <h2 id="pharmacy">胡庆余堂</h2>
      <p>胡庆余堂是胡雪岩创办的药号，以"戒欺"为店训，"采办务真，修制务精"。至今仍是中华老字号，传承着胡雪岩的商业精神。</p>
      
      <h2 id="rise-fall">兴衰启示</h2>
      <p>胡雪岩的兴衰给我们的启示：</p>
      <ul>
        <li>商业成功需要政治智慧，但过度依赖政治是危险的</li>
        <li>诚信经营是长久之道</li>
        <li>居安思危，不可盲目扩张</li>
        <li>财富要取之有道，用之有方</li>
      </ul>
      
      <h2 id="conclusion">总结</h2>
      <p>胡雪岩的一生是传奇的一生。他的商业智慧、为人处世之道，至今仍值得我们学习和借鉴。正如他所说："天下没有难做的生意，只有不会做生意的人。"</p>
    `,
    authorId: '1',
    authorName: '胡雪岩',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan',
    authorBio: '红顶商人，徽商代表，胡庆余堂创始人',
    createTime: '2025-12-20 10:00',
    viewCount: 18680,
    likeCount: 2340,
    collectCount: 678,
    isLiked: false,
    isCollected: false,
    isPaid: true,
    hasPurchased: false,
    price: 29.9,
    tags: ['胡雪岩', '徽商', '商业智慧']
  },
  '2': {
    id: '2',
    title: '徽商精神：诚信为本，以义取利',
    content: `
      <h2 id="intro">引言</h2>
      <p>徽商，是中国历史上最具影响力的商帮之一。他们以诚信经营、以义取利的商业理念，在明清时期称雄商界数百年。</p>
      
      <h2 id="spirit">徽商精神的核心</h2>
      <p>徽商精神的核心价值观包括：</p>
      <ul>
        <li><strong>诚信为本</strong>：徽商认为"诚招天下客，誉从信中来"</li>
        <li><strong>以义取利</strong>：追求利润但不忘道义</li>
        <li><strong>勤俭持家</strong>：节俭经营，积少成多</li>
        <li><strong>团结互助</strong>：同乡互助，共同发展</li>
      </ul>
      
      <h2 id="modern">对现代商业的启示</h2>
      <p>徽商精神对现代商业仍有重要启示：</p>
      <ul>
        <li>诚信是企业的立身之本</li>
        <li>社会责任与商业利益并重</li>
        <li>长期主义胜过短期投机</li>
        <li>文化传承是企业的软实力</li>
      </ul>
      
      <h2 id="conclusion">总结</h2>
      <p>徽商精神是中华商业文化的瑰宝，值得我们传承和发扬。在当今商业社会，我们更需要这种诚信经营、以义取利的精神。</p>
    `,
    authorId: '1',
    authorName: '胡雪岩',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan',
    authorBio: '红顶商人，徽商代表，胡庆余堂创始人',
    createTime: '2025-12-18 14:20',
    viewCount: 12450,
    likeCount: 1890,
    collectCount: 456,
    isLiked: false,
    isCollected: false,
    isPaid: false,
    hasPurchased: false,
    price: 0,
    tags: ['徽商文化', '商业伦理', '传统文化']
  },
  '11': {
    id: '11',
    title: '收复新疆：一场艰苦卓绝的战争',
    content: `
      <h2 id="intro">引言</h2>
      <p>1876年，左宗棠率领湘军西征，开始了收复新疆的伟大征程。这场战争历时数年，最终成功收复了被阿古柏侵占的新疆领土。</p>
      
      <h2 id="background">历史背景</h2>
      <p>1865年，中亚浩罕国军官阿古柏侵入新疆，建立了所谓的"哲德沙尔汗国"。清政府内部对是否收复新疆存在"海防"与"塞防"之争。</p>
      
      <h2 id="preparation">西征准备</h2>
      <p>左宗棠力主收复新疆，并做了充分准备：</p>
      <ul>
        <li>筹集军饷，向胡雪岩借款</li>
        <li>训练军队，整顿军纪</li>
        <li>准备粮草，建立后勤</li>
        <li>抬棺出征，表明决心</li>
      </ul>
      
      <h2 id="campaign">战争过程</h2>
      <p>左宗棠采取"缓进急战"的策略，先北后南，逐步收复失地。经过艰苦战斗，最终全面收复新疆。</p>
      
      <h2 id="significance">历史意义</h2>
      <p>收复新疆的意义重大：</p>
      <ul>
        <li>维护了国家领土完整</li>
        <li>巩固了西北边防</li>
        <li>促进了新疆建省</li>
        <li>展现了民族气节</li>
      </ul>
      
      <h2 id="conclusion">总结</h2>
      <p>左宗棠收复新疆，是中国近代史上的壮举。他的爱国精神和军事才能，值得后人永远铭记。</p>
    `,
    authorId: '2',
    authorName: '左宗棠',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang',
    authorBio: '晚清名臣，收复新疆的民族英雄',
    createTime: '2025-12-17 10:00',
    viewCount: 12340,
    likeCount: 1890,
    collectCount: 345,
    isLiked: false,
    isCollected: false,
    isPaid: false,
    hasPurchased: false,
    price: 0,
    tags: ['历史', '军事', '新疆']
  },
  '12': {
    id: '12',
    title: '洋务运动中的福建船政局',
    content: `
      <h2 id="intro">引言</h2>
      <p>福建船政局是洋务运动时期创办的重要军事工业企业，也是中国近代海军的摇篮。左宗棠在担任闽浙总督期间，主持创办了这一重要机构。</p>
      
      <h2 id="establishment">创办背景</h2>
      <p>1866年，左宗棠上奏朝廷，提出在福州马尾创办船政局，制造轮船，培养人才。这是洋务运动"自强"的重要举措。</p>
      
      <h2 id="achievements">主要成就</h2>
      <p>福建船政局取得了显著成就：</p>
      <ul>
        <li>建造了中国第一艘千吨级轮船</li>
        <li>培养了大批海军人才</li>
        <li>创办了船政学堂</li>
        <li>派遣留学生赴欧美学习</li>
      </ul>
      
      <h2 id="talents">人才培养</h2>
      <p>船政学堂培养了众多杰出人才，如严复、刘步蟾、林泰曾等，他们成为中国近代化的先驱。</p>
      
      <h2 id="significance">历史意义</h2>
      <p>福建船政局的创办，标志着中国开始学习西方先进技术，是中国近代化进程中的重要里程碑。</p>
      
      <h2 id="conclusion">总结</h2>
      <p>福建船政局虽然最终未能挽救清朝的命运，但它在中国近代化进程中的作用不可磨灭。</p>
    `,
    authorId: '2',
    authorName: '左宗棠',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang',
    authorBio: '晚清名臣，洋务运动的重要推动者',
    createTime: '2025-12-15 14:30',
    viewCount: 9870,
    likeCount: 1450,
    collectCount: 298,
    isLiked: false,
    isCollected: false,
    isPaid: false,
    hasPurchased: false,
    price: 0,
    tags: ['洋务运动', '近代化', '海军']
  },
  '13': {
    id: '13',
    title: '轮船招商局的创办历程',
    content: `
      <h2 id="intro">引言</h2>
      <p>轮船招商局是中国第一家轮船公司，由盛宣怀在李鸿章的支持下创办。它标志着中国近代航运业的开端。</p>
      
      <h2 id="background">创办背景</h2>
      <p>19世纪70年代，外国轮船公司垄断了中国的航运市场。为了"收回利权"，清政府决定创办自己的轮船公司。</p>
      
      <h2 id="establishment">创办过程</h2>
      <p>1872年，盛宣怀受李鸿章委派，筹办轮船招商局：</p>
      <ul>
        <li>采用"官督商办"模式</li>
        <li>招募商股，筹集资金</li>
        <li>购买轮船，开辟航线</li>
        <li>与外国公司竞争</li>
      </ul>
      
      <h2 id="development">发展历程</h2>
      <p>轮船招商局经过艰苦经营，逐步发展壮大，成为中国最大的航运企业。它不仅经营客货运输，还涉足煤矿、纺织等行业。</p>
      
      <h2 id="significance">历史意义</h2>
      <p>轮船招商局的创办具有重要意义：</p>
      <ul>
        <li>打破了外国公司的垄断</li>
        <li>促进了民族工商业发展</li>
        <li>培养了近代企业管理人才</li>
        <li>探索了"官督商办"模式</li>
      </ul>
      
      <h2 id="conclusion">总结</h2>
      <p>轮船招商局是中国近代企业的先驱，它的创办和发展，为后来的民族工业奠定了基础。</p>
    `,
    authorId: '3',
    authorName: '盛宣怀',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai',
    authorBio: '洋务运动代表人物，中国近代工业的奠基人',
    createTime: '2025-12-14 09:00',
    viewCount: 8760,
    likeCount: 1230,
    collectCount: 267,
    isLiked: false,
    isCollected: false,
    isPaid: false,
    hasPurchased: false,
    price: 0,
    tags: ['洋务运动', '航运', '近代企业']
  },
  '14': {
    id: '14',
    title: '创办北洋大学堂的初衷',
    content: `
      <h2 id="intro">引言</h2>
      <p>北洋大学堂（今天津大学）是中国第一所现代大学，由盛宣怀于1895年创办。它开创了中国高等教育的新纪元。</p>
      
      <h2 id="background">创办背景</h2>
      <p>甲午战争的惨败，让盛宣怀深刻认识到人才培养的重要性。他认为："自强首在储才，储才必先兴学。"</p>
      
      <h2 id="establishment">创办过程</h2>
      <p>盛宣怀克服重重困难，创办了北洋大学堂：</p>
      <ul>
        <li>选址天津，便于招生</li>
        <li>聘请中外教师</li>
        <li>设置工程、矿冶等专业</li>
        <li>采用西方教学模式</li>
      </ul>
      
      <h2 id="features">办学特色</h2>
      <p>北洋大学堂的办学特色鲜明：</p>
      <ul>
        <li>注重实学，培养工程技术人才</li>
        <li>中西结合，兼顾传统与现代</li>
        <li>严格管理，学风优良</li>
        <li>产学结合，服务实业</li>
      </ul>
      
      <h2 id="influence">深远影响</h2>
      <p>北洋大学堂培养了大批优秀人才，如茅以升、张太雷等，他们成为中国近代化的中坚力量。</p>
      
      <h2 id="conclusion">总结</h2>
      <p>盛宣怀创办北洋大学堂，是中国教育史上的里程碑事件。它为中国高等教育的发展开辟了道路。</p>
    `,
    authorId: '3',
    authorName: '盛宣怀',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai',
    authorBio: '洋务运动代表人物，中国近代教育的先驱',
    createTime: '2025-12-12 15:20',
    viewCount: 7890,
    likeCount: 1120,
    collectCount: 234,
    isLiked: false,
    isCollected: false,
    isPaid: false,
    hasPurchased: false,
    price: 0,
    tags: ['教育', '大学', '近代化']
  },
  '15': {
    id: '15',
    title: '大生纱厂的创办与发展',
    content: `
      <h2 id="intro">引言</h2>
      <p>大生纱厂是张謇创办的中国近代民族工业的典范企业。它的成功，标志着中国民族资本主义的兴起。</p>
      
      <h2 id="background">创办背景</h2>
      <p>1895年，张謇中状元后，毅然弃官从商，决心"实业救国"。他选择在家乡南通创办纱厂。</p>
      
      <h2 id="establishment">创办过程</h2>
      <p>张謇克服资金、技术、人才等困难，终于建成大生纱厂：</p>
      <ul>
        <li>筹集资金，招募股东</li>
        <li>引进设备，聘请技师</li>
        <li>培训工人，建立制度</li>
        <li>开拓市场，树立品牌</li>
      </ul>
      
      <h2 id="development">发展历程</h2>
      <p>大生纱厂经营有方，迅速发展壮大：</p>
      <ul>
        <li>产品质量优良，畅销全国</li>
        <li>管理科学，效益显著</li>
        <li>扩大规模，建立分厂</li>
        <li>多元经营，涉足多个行业</li>
      </ul>
      
      <h2 id="significance">历史意义</h2>
      <p>大生纱厂的成功具有重要意义：</p>
      <ul>
        <li>证明了民族工业的可行性</li>
        <li>推动了南通的现代化</li>
        <li>培养了工业管理人才</li>
        <li>树立了企业家精神</li>
      </ul>
      
      <h2 id="conclusion">总结</h2>
      <p>张謇创办大生纱厂，是中国近代民族工业发展的典范。他的"实业救国"理想，激励着一代又一代企业家。</p>
    `,
    authorId: '4',
    authorName: '张謇',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian',
    authorBio: '状元实业家，中国近代民族工业的开拓者',
    createTime: '2025-12-10 11:00',
    viewCount: 6780,
    likeCount: 980,
    collectCount: 198,
    isLiked: false,
    isCollected: false,
    isPaid: false,
    hasPurchased: false,
    price: 0,
    tags: ['实业', '纺织', '民族工业']
  },
  '16': {
    id: '16',
    title: '状元办实业的心路历程',
    content: `
      <h2 id="intro">引言</h2>
      <p>张謇，清朝状元，却选择弃官从商，投身实业。这一选择在当时引起了巨大争议，但他坚定地走上了"实业救国"的道路。</p>
      
      <h2 id="choice">艰难抉择</h2>
      <p>中状元后，张謇面临人生的重大抉择：</p>
      <ul>
        <li>做官：光宗耀祖，前途无量</li>
        <li>经商：风险巨大，饱受非议</li>
      </ul>
      <p>经过深思熟虑，他选择了后者，因为他认为"实业才能救国"。</p>
      
      <h2 id="challenges">创业艰辛</h2>
      <p>张謇创业过程中遇到了诸多困难：</p>
      <ul>
        <li>资金短缺，四处筹款</li>
        <li>技术落后，学习西方</li>
        <li>观念陈旧，改变思想</li>
        <li>官商勾结，艰难生存</li>
      </ul>
      
      <h2 id="philosophy">经营理念</h2>
      <p>张謇的经营理念独具特色：</p>
      <ul>
        <li>父教育而母实业</li>
        <li>企业利润回馈社会</li>
        <li>注重员工福利</li>
        <li>推动地方建设</li>
      </ul>
      
      <h2 id="legacy">历史贡献</h2>
      <p>张謇不仅创办了成功的企业，还：</p>
      <ul>
        <li>创办学校，普及教育</li>
        <li>修建道路，改善交通</li>
        <li>兴办慈善，救济贫困</li>
        <li>推动南通成为"模范县"</li>
      </ul>
      
      <h2 id="conclusion">总结</h2>
      <p>张謇从状元到实业家的转变，体现了他的家国情怀和社会责任感。他的"实业救国"理想，至今仍有现实意义。</p>
    `,
    authorId: '4',
    authorName: '张謇',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian',
    authorBio: '状元实业家，实业救国的践行者',
    createTime: '2025-12-08 10:30',
    viewCount: 5890,
    likeCount: 870,
    collectCount: 176,
    isLiked: false,
    isCollected: false,
    isPaid: false,
    hasPurchased: false,
    price: 0,
    tags: ['人物', '实业救国', '转型']
  },
  '3': {
    id: '3',
    title: '深入理解Spring Boot 3.0微服务架构设计',
    // 付费文章预览内容（未购买时显示）
    previewContent: `
      <h2 id="intro">引言</h2>
      <p>微服务架构已经成为现代企业应用开发的主流选择。Spring Boot 3.0为微服务开发提供了强大的支持。</p>
      
      <h2 id="architecture">架构设计原则</h2>
      <p>本文将深入探讨微服务架构的核心设计原则，包括服务拆分、通信机制、数据管理等方面。</p>
      
      <div class="locked-content">
        <p>🔒 <em>以下为付费内容，购买后可查看完整文章...</em></p>
      </div>
    `,
    // 付费文章完整内容（购买后显示）
    content: `
      <h2 id="intro">引言</h2>
      <p>微服务架构已经成为现代企业应用开发的主流选择。Spring Boot 3.0为微服务开发提供了强大的支持。本文将从胡雪岩的商业智慧出发，探讨如何构建高效的微服务架构。</p>
      
      <h2 id="architecture">架构设计原则</h2>
      <p>正如胡雪岩所言："做生意要讲究诚信，做架构也要讲究可靠。"微服务架构的核心设计原则包括：</p>
      <ul>
        <li><strong>单一职责原则</strong>：每个服务只负责一个业务领域</li>
        <li><strong>服务自治</strong>：服务独立部署、独立扩展</li>
        <li><strong>去中心化</strong>：避免单点故障</li>
        <li><strong>容错设计</strong>：熔断、降级、限流</li>
      </ul>
      
      <h2 id="service-split">服务拆分策略</h2>
      <p>胡雪岩的钱庄遍布各地，每个分号独立运营却又协同合作。微服务拆分也应如此：</p>
      <ul>
        <li>按业务领域拆分（DDD领域驱动设计）</li>
        <li>按团队组织拆分（康威定律）</li>
        <li>按变更频率拆分</li>
        <li>按性能需求拆分</li>
      </ul>
      
      <h2 id="communication">服务通信机制</h2>
      <p>微服务之间的通信方式主要有：</p>
      <ul>
        <li><strong>同步通信</strong>：REST API、gRPC</li>
        <li><strong>异步通信</strong>：消息队列（RabbitMQ、Kafka）</li>
        <li><strong>事件驱动</strong>：Event Sourcing、CQRS</li>
      </ul>
      
      <h2 id="data-management">数据管理</h2>
      <p>每个微服务应该拥有自己的数据库，实现数据隔离。跨服务数据一致性可通过：</p>
      <ul>
        <li>Saga模式</li>
        <li>事件溯源</li>
        <li>最终一致性</li>
      </ul>
      
      <h2 id="springboot">Spring Boot 3.0 实践</h2>
      <p>Spring Boot 3.0 带来了许多新特性：</p>
      <ul>
        <li>原生镜像支持（GraalVM）</li>
        <li>Jakarta EE 9+ 支持</li>
        <li>改进的可观测性</li>
        <li>更好的安全性</li>
      </ul>
      
      <h2 id="conclusion">总结</h2>
      <p>正如胡雪岩能够建立起庞大的商业帝国，良好的微服务架构也能支撑起复杂的企业应用。关键在于合理的服务拆分、可靠的通信机制和完善的数据管理策略。</p>
    `,
    authorId: '3',
    authorName: 'Java架构师',
    authorAvatar: 'https://picsum.photos/100/100?random=12',
    authorBio: '10年Java开发经验，专注于企业级架构设计',
    createTime: '2025-12-15 09:00',
    viewCount: 6750,
    likeCount: 445,
    collectCount: 156,
    isLiked: false,
    isCollected: false,
    isPaid: true,
    hasPurchased: false,
    price: 9.9,
    tags: ['Java', 'Spring Boot', '微服务']
  },
  '4': {
    id: '4',
    title: '程序员的健康生活指南：如何在高压工作中保持身心健康',
    content: `
      <h2 id="intro">引言</h2>
      <p>作为程序员，我们每天面对电脑工作8小时甚至更长时间。长期久坐、熬夜加班、饮食不规律，这些都在悄悄侵蚀着我们的健康。</p>
      
      <h2 id="physical">身体健康</h2>
      <p>保持身体健康是高效工作的基础：</p>
      <ul>
        <li><strong>定时休息</strong>：每工作45分钟，起身活动5-10分钟</li>
        <li><strong>正确坐姿</strong>：显示器与眼睛平齐，椅子高度适中</li>
        <li><strong>适量运动</strong>：每周至少3次有氧运动，每次30分钟以上</li>
        <li><strong>护眼习惯</strong>：20-20-20法则，每20分钟看20英尺外20秒</li>
      </ul>
      
      <h2 id="mental">心理健康</h2>
      <p>程序员面临的压力不仅来自工作，还有技术更新的焦虑：</p>
      <ul>
        <li>学会说"不"，合理安排工作量</li>
        <li>培养工作之外的兴趣爱好</li>
        <li>保持良好的社交关系</li>
        <li>必要时寻求专业心理咨询</li>
      </ul>
      
      <h2 id="diet">饮食建议</h2>
      <p>健康的饮食习惯对程序员尤为重要：</p>
      <ul>
        <li>少喝咖啡和碳酸饮料，多喝水</li>
        <li>避免外卖和快餐，尽量自己做饭</li>
        <li>多吃蔬菜水果，补充维生素</li>
        <li>规律进餐，不要饿着肚子写代码</li>
      </ul>
      
      <h2 id="conclusion">总结</h2>
      <p>健康是1，其他都是0。没有健康的身体，再高的薪水、再好的技术都是空谈。从今天开始，关注自己的健康吧！</p>
    `,
    authorId: '4',
    authorName: '健康达人',
    authorAvatar: 'https://picsum.photos/100/100?random=13',
    authorBio: '关注程序员身心健康，倡导健康生活方式',
    createTime: '2025-12-12 08:00',
    viewCount: 15230,
    likeCount: 1256,
    collectCount: 456,
    isLiked: false,
    isCollected: false,
    isPaid: false,
    hasPurchased: false,
    price: 0,
    tags: ['健康', '生活', '职场']
  },
  '5': {
    id: '5',
    title: '胡雪岩的商业智慧：从学徒到红顶商人',
    previewContent: `
      <h2 id="intro">引言</h2>
      <p>胡雪岩（1823-1885），清朝著名徽商，被誉为"红顶商人"。他从一个钱庄学徒起步，最终成为清朝首富，其传奇经历至今仍被人们津津乐道。</p>
      
      <h2 id="early">早年经历</h2>
      <p>胡雪岩出生于安徽绩溪，12岁便到杭州一家钱庄当学徒。他勤奋好学，善于观察，很快就掌握了钱庄的经营之道。</p>
      
      <div class="locked-content">
        <p>🔒 <em>以下为付费内容，购买后可查看完整文章...</em></p>
      </div>
    `,
    content: `
      <h2 id="intro">引言</h2>
      <p>胡雪岩（1823-1885），清朝著名徽商，被誉为"红顶商人"。他从一个钱庄学徒起步，最终成为清朝首富，其传奇经历至今仍被人们津津乐道。</p>
      
      <h2 id="early">早年经历</h2>
      <p>胡雪岩出生于安徽绩溪，12岁便到杭州一家钱庄当学徒。他勤奋好学，善于观察，很快就掌握了钱庄的经营之道。</p>
      
      <h2 id="business">经商之道</h2>
      <p>胡雪岩的成功离不开他独特的经商智慧：</p>
      <ul>
        <li><strong>诚信为本</strong>："做生意先做人，做人先讲诚信"</li>
        <li><strong>广结善缘</strong>：善于结交各方人士，建立广泛的人脉网络</li>
        <li><strong>审时度势</strong>：善于把握时机，在乱世中寻找商机</li>
        <li><strong>回馈社会</strong>：创办胡庆余堂，济世救人</li>
      </ul>
      
      <h2 id="pharmacy">胡庆余堂</h2>
      <p>胡庆余堂是胡雪岩创办的药号，以"戒欺"为店训，"采办务真，修制务精"。至今仍是中华老字号，传承着胡雪岩的商业精神。</p>
      
      <h2 id="rise-fall">兴衰启示</h2>
      <p>胡雪岩的兴衰给我们的启示：</p>
      <ul>
        <li>商业成功需要政治智慧，但过度依赖政治是危险的</li>
        <li>诚信经营是长久之道</li>
        <li>居安思危，不可盲目扩张</li>
        <li>财富要取之有道，用之有方</li>
      </ul>
      
      <h2 id="conclusion">总结</h2>
      <p>胡雪岩的一生是传奇的一生。他的商业智慧、为人处世之道，至今仍值得我们学习和借鉴。正如他所说："天下没有难做的生意，只有不会做生意的人。"</p>
    `,
    authorId: '5',
    authorName: '胡雪岩',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan',
    authorBio: '红顶商人，徽商代表，胡庆余堂创始人',
    createTime: '2025-12-10 10:00',
    viewCount: 18680,
    likeCount: 2340,
    collectCount: 678,
    isLiked: false,
    isCollected: false,
    isPaid: true,
    hasPurchased: false,
    price: 29.9,
    tags: ['胡雪岩', '徽商', '商业智慧']
  },
  '6': {
    id: '6',
    title: '2025年最值得学习的编程语言排行榜',
    content: `
      <h2 id="intro">引言</h2>
      <p>技术在不断发展，编程语言的流行度也在变化。选择正确的编程语言学习，可以让你的职业发展事半功倍。</p>
      
      <h2 id="python">1. Python</h2>
      <p>Python继续保持其在AI、数据科学和自动化领域的统治地位：</p>
      <ul>
        <li>简洁易学，适合初学者</li>
        <li>AI和机器学习的首选语言</li>
        <li>丰富的库和框架生态</li>
        <li>薪资水平持续走高</li>
      </ul>
      
      <h2 id="javascript">2. JavaScript/TypeScript</h2>
      <p>Web开发的基石，全栈开发的必备技能：</p>
      <ul>
        <li>前端开发的唯一选择</li>
        <li>Node.js让JS可以做后端</li>
        <li>TypeScript提供类型安全</li>
        <li>React、Vue、Angular三大框架</li>
      </ul>
      
      <h2 id="rust">3. Rust</h2>
      <p>系统编程的新星，安全性和性能兼得：</p>
      <ul>
        <li>内存安全，无需垃圾回收</li>
        <li>性能媲美C/C++</li>
        <li>WebAssembly的最佳选择</li>
        <li>越来越多的大公司采用</li>
      </ul>
      
      <h2 id="go">4. Go</h2>
      <p>云原生时代的宠儿：</p>
      <ul>
        <li>简单易学，编译速度快</li>
        <li>天生支持并发</li>
        <li>Docker、Kubernetes都用Go写的</li>
        <li>微服务开发的热门选择</li>
      </ul>
      
      <h2 id="conclusion">总结</h2>
      <p>选择编程语言要根据自己的职业规划和兴趣。没有最好的语言，只有最适合的语言。建议先精通一门，再扩展其他。</p>
    `,
    authorId: '1',
    authorName: '技术先锋',
    authorAvatar: 'https://picsum.photos/100/100?random=10',
    authorBio: '资深前端工程师，专注于前沿技术研究和分享',
    createTime: '2025-12-08 15:30',
    viewCount: 23450,
    likeCount: 1890,
    collectCount: 567,
    isLiked: false,
    isCollected: false,
    isPaid: false,
    hasPurchased: false,
    price: 0,
    tags: ['编程语言', '技术趋势', '学习']
  }
}

// 获取已购买文章列表
const getPurchasedArticles = () => {
  const purchased = getItem('purchasedArticles')
  return purchased ? JSON.parse(purchased) : []
}

// 保存已购买文章
const savePurchasedArticle = (articleId) => {
  const purchased = getPurchasedArticles()
  if (!purchased.includes(articleId)) {
    purchased.push(articleId)
    setItem('purchasedArticles', JSON.stringify(purchased))
  }
}

// 模拟评论数据
const mockComments = [
  {
    id: 1,
    nickname: '前端小白',
    avatar: 'https://picsum.photos/100/100?random=20',
    content: '写得太好了！AI确实在改变我们的开发方式，期待更多这样的文章。',
    createTime: '2025-12-20 12:30',
    likeCount: 23,
    replies: [
      {
        id: 11,
        nickname: '技术先锋',
        avatar: 'https://picsum.photos/100/100?random=10',
        content: '感谢支持！后续会继续分享更多前沿技术内容。',
        createTime: '2025-12-20 13:00',
        likeCount: 5
      }
    ]
  },
  {
    id: 2,
    nickname: '全栈开发者',
    avatar: 'https://picsum.photos/100/100?random=21',
    content: '非常认同文章的观点，AI辅助编程确实大大提高了开发效率。不过也要注意不能过度依赖。',
    createTime: '2025-12-20 11:45',
    likeCount: 18,
    replies: []
  },
  {
    id: 3,
    nickname: '学习中的菜鸟',
    avatar: 'https://picsum.photos/100/100?random=22',
    content: '作为新手，感觉AI工具对我帮助很大，可以更快地学习和理解代码。',
    createTime: '2025-12-20 10:50',
    likeCount: 12,
    replies: []
  }
]

// 加载文章详情
const loadArticle = async () => {
  const id = route.params.id
  
  try {
    // 从API加载文章详情
    const { getArticleDetail } = await import('@/api/article')
    const articleData = await getArticleDetail(id)
    
    // 检查是否已购买（从localStorage读取）
    const purchasedArticles = getPurchasedArticles()
    const hasPurchased = purchasedArticles.includes(id)
    
    // 如果用户已登录，从后端获取点赞和收藏状态
    let isLiked = false
    let isCollected = false
    
    if (userStore.isLoggedIn) {
      try {
        const { getLikeStatus, getFavoriteStatus } = await import('@/api/interaction')
        const [likeRes, favoriteRes] = await Promise.all([
          getLikeStatus(id),
          getFavoriteStatus(id)
        ])
        // 响应拦截器已经解包data，直接使用返回的对象
        isLiked = likeRes?.liked || false
        isCollected = favoriteRes?.favorited || false
      } catch (error) {
        console.error('获取互动状态失败:', error)
      }
    }
    
    // 设置文章数据
    article.value = {
      id: articleData.id,
      title: articleData.title,
      content: articleData.content,
      summary: articleData.summary,
      coverImage: articleData.coverImage,
      authorId: articleData.author?.id,
      authorName: articleData.author?.nickname || '未知作者',
      authorAvatar: articleData.author?.avatar,
      viewCount: articleData.viewCount || 0,
      likeCount: articleData.likeCount || 0,  // 确保从后端获取真实的点赞数
      collectCount: articleData.favoriteCount || 0,  // 确保从后端获取真实的收藏数
      commentCount: articleData.commentCount || 0,
      isPaid: articleData.isPaid,
      price: articleData.price,
      categoryId: articleData.categoryId,
      categoryName: articleData.categoryName,
      tags: articleData.tags || [],
      createTime: articleData.createdAt,
      // 判断是否已购买：
      // 1. 如果内容不包含付费提示，说明已购买或不是付费文章
      // 2. 如果是付费文章但内容完整（不包含付费提示），说明已购买
      hasPurchased: articleData.isPaid ? !(articleData.content && articleData.content.includes('🔒 以下为付费内容')) : true,
      isLiked,
      isCollected
    }
    
    // 检查是否已关注作者（从后端API加载）
    if (userStore.isLoggedIn && articleData.author?.id) {
      try {
        const { getFollowStatus } = await import('@/api/interaction')
        const response = await getFollowStatus(articleData.author.id)
        isFollowingAuthor.value = response.following || false
        console.log('作者关注状态已加载:', isFollowingAuthor.value)
      } catch (error) {
        console.error('加载作者关注状态失败:', error)
        isFollowingAuthor.value = false
      }
    } else {
      isFollowingAuthor.value = false
    }
    
    // 记录浏览历史到后端（如果已登录）
    if (userStore.isLoggedIn) {
      try {
        const { recordBrowseHistory } = await import('@/api/article')
        await recordBrowseHistory(id)
        console.log('浏览历史已记录到后端')
      } catch (error) {
        console.error('记录浏览历史失败:', error)
        // 不影响页面显示，静默失败
      }
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
    // 可以跳转回首页或显示错误页面
  }
}

// 加载评论
const loadComments = async () => {
  try {
    // 调用后端API获取评论列表
    const response = await fetch(`http://localhost:8080/api/comments/article/${route.params.id}?page=0&size=100`)
    const data = await response.json()
    
    console.log('加载评论数据:', data)
    
    if (data.success && data.data) {
      const topLevelComments = data.data.comments.map(comment => ({
        id: comment.id,
        userId: comment.userId, // 添加 userId 字段用于权限判断
        nickname: comment.userNickname || '匿名用户',
        avatar: comment.userAvatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default',
        content: comment.content,
        createTime: comment.createdAt,
        likeCount: 0,
        replies: []
      }))
      
      console.log('顶级评论数量:', topLevelComments.length)
      
      // 为每个顶级评论加载回复
      for (const comment of topLevelComments) {
        try {
          console.log(`加载评论 ${comment.id} 的回复...`)
          const replyResponse = await fetch(`http://localhost:8080/api/comments/${comment.id}/replies`)
          const replyData = await replyResponse.json()
          
          console.log(`评论 ${comment.id} 的回复数据:`, replyData)
          
          if (replyData.success && replyData.data) {
            comment.replies = replyData.data.map(reply => ({
              id: reply.id,
              userId: reply.userId,
              nickname: reply.userNickname || '匿名用户',
              avatar: reply.userAvatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default',
              content: reply.content,
              createTime: reply.createdAt,
              likeCount: 0,
              replies: []
            }))
            console.log(`评论 ${comment.id} 加载了 ${comment.replies.length} 条回复`)
          }
        } catch (error) {
          console.error(`加载评论 ${comment.id} 的回复失败:`, error)
        }
      }
      
      console.log('最终评论列表:', topLevelComments)
      comments.value = topLevelComments
      commentTotal.value = data.data.totalElements || comments.value.length
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    // 如果加载失败，使用空数组
    comments.value = []
    commentTotal.value = 0
  }
}

// 点赞
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    const { toggleLike } = await import('@/api/interaction')
    const response = await toggleLike(article.value.id)
    
    // 响应拦截器已经解包data，response就是返回的数据对象
    article.value.isLiked = response.liked
    
    // 重新加载文章以获取最新的点赞数
    const { getArticleDetail } = await import('@/api/article')
    const articleData = await getArticleDetail(article.value.id)
    article.value.likeCount = articleData.likeCount || 0
    
    // 更新localStorage中的点赞列表（按用户ID隔离）
    const currentUserId = userStore.userInfo?.id
    const allLikedArticles = JSON.parse(getItem('likedArticles') || '{}')
    const likedArticles = allLikedArticles[currentUserId] || []
    
    if (article.value.isLiked) {
      // 添加到点赞列表
      const articleInfo = {
        id: article.value.id,
        title: article.value.title,
        summary: article.value.summary || article.value.content?.substring(0, 100) + '...',
        coverImage: article.value.coverImage || 'https://picsum.photos/800/400?random=' + article.value.id,
        authorId: article.value.authorId,
        authorName: article.value.authorName,
        authorAvatar: article.value.authorAvatar,
        viewCount: article.value.viewCount,
        likeCount: article.value.likeCount,
        commentCount: article.value.commentCount || 0,
        tags: article.value.tags || [],
        createTime: article.value.createTime,
        isPaid: article.value.isPaid,
        price: article.value.price
      }
      
      // 按用户ID存储点赞列表
      const currentUserId = userStore.userInfo?.id
      if (currentUserId) {
        const allLikedArticles = JSON.parse(getItem('likedArticles') || '{}')
        if (!allLikedArticles[currentUserId]) {
          allLikedArticles[currentUserId] = []
        }
        // 检查是否已存在
        if (!allLikedArticles[currentUserId].find(a => a.id === article.value.id)) {
          allLikedArticles[currentUserId].unshift(articleInfo)
          setItem('likedArticles', JSON.stringify(allLikedArticles))
        }
      }
    } else {
      // 从点赞列表移除
      const currentUserId = userStore.userInfo?.id
      if (currentUserId) {
        const allLikedArticles = JSON.parse(getItem('likedArticles') || '{}')
        if (allLikedArticles[currentUserId]) {
          allLikedArticles[currentUserId] = allLikedArticles[currentUserId].filter(a => a.id !== article.value.id)
          setItem('likedArticles', JSON.stringify(allLikedArticles))
        }
      }
    }
    
    ElMessage.success(response.message || (article.value.isLiked ? '点赞成功' : '已取消点赞'))
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 收藏
const handleCollect = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    const { toggleFavorite } = await import('@/api/interaction')
    const response = await toggleFavorite(article.value.id)
    
    // 响应拦截器已经解包data，response就是返回的数据对象
    article.value.isCollected = response.favorited
    
    // 重新加载文章以获取最新的收藏数
    const { getArticleDetail } = await import('@/api/article')
    const articleData = await getArticleDetail(article.value.id)
    article.value.collectCount = articleData.favoriteCount || 0
    
    // 更新localStorage中的收藏列表（按用户ID隔离）
    const currentUserId = userStore.userInfo?.id
    const allCollectedArticles = JSON.parse(getItem('collectedArticles') || '{}')
    const collectedArticles = allCollectedArticles[currentUserId] || []
    
    if (article.value.isCollected) {
      // 添加到收藏列表
      const articleInfo = {
        id: article.value.id,
        title: article.value.title,
        summary: article.value.summary || article.value.content?.substring(0, 100) + '...',
        coverImage: article.value.coverImage || 'https://picsum.photos/800/400?random=' + article.value.id,
        authorId: article.value.authorId,
        authorName: article.value.authorName,
        authorAvatar: article.value.authorAvatar,
        viewCount: article.value.viewCount,
        likeCount: article.value.likeCount,
        commentCount: article.value.commentCount || 0,
        tags: article.value.tags || [],
        createTime: article.value.createTime,
        isPaid: article.value.isPaid,
        price: article.value.price
      }
      
      // 按用户ID存储收藏列表
      const currentUserId = userStore.userInfo?.id
      if (currentUserId) {
        const allCollectedArticles = JSON.parse(getItem('collectedArticles') || '{}')
        if (!allCollectedArticles[currentUserId]) {
          allCollectedArticles[currentUserId] = []
        }
        // 检查是否已存在
        if (!allCollectedArticles[currentUserId].find(a => a.id === article.value.id)) {
          allCollectedArticles[currentUserId].unshift(articleInfo)
          setItem('collectedArticles', JSON.stringify(allCollectedArticles))
        }
      }
    } else {
      // 从收藏列表移除
      const currentUserId = userStore.userInfo?.id
      if (currentUserId) {
        const allCollectedArticles = JSON.parse(getItem('collectedArticles') || '{}')
        if (allCollectedArticles[currentUserId]) {
          allCollectedArticles[currentUserId] = allCollectedArticles[currentUserId].filter(a => a.id !== article.value.id)
          setItem('collectedArticles', JSON.stringify(allCollectedArticles))
        }
      }
    }
    
    ElMessage.success(response.message || (article.value.isCollected ? '收藏成功' : '已取消收藏'))
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 购买文章
const handlePurchase = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  const id = route.params.id
  
  try {
    // 调用购买API
    await purchaseArticle({ articleId: id })
    
    // 更新购买状态
    article.value.hasPurchased = true
    
    // 重新加载文章详情以获取完整信息
    const { getArticleDetail } = await import('@/api/article')
    const articleData = await getArticleDetail(id)
    
    // 显示完整内容
    if (articleData.content) {
      article.value.content = articleData.content
    }
    
    // 将文章信息添加到已购文章列表
    const purchasedArticles = JSON.parse(getItem('purchasedArticles') || '[]')
    const articleInfo = {
      id: articleData.id || article.value.id,
      title: articleData.title || article.value.title,
      summary: articleData.summary || article.value.summary || articleData.content?.substring(0, 100) + '...',
      coverImage: articleData.coverImage || article.value.coverImage || `https://picsum.photos/800/400?random=${articleData.id}`,
      authorId: articleData.author?.id || article.value.authorId,
      authorName: articleData.author?.nickname || article.value.authorName,
      authorAvatar: articleData.author?.avatar || article.value.authorAvatar,
      viewCount: articleData.viewCount || article.value.viewCount || 0,
      likeCount: articleData.likeCount || article.value.likeCount || 0,
      commentCount: articleData.commentCount || article.value.commentCount || 0,
      tags: articleData.tags || article.value.tags || [],
      createTime: articleData.createdAt || article.value.createTime,
      isPaid: true,
      price: articleData.price || article.value.price,
      purchaseTime: new Date().toISOString()
    }
    
    console.log('保存已购文章信息:', articleInfo)
    
    // 检查是否已存在
    const existingIndex = purchasedArticles.findIndex(a => a.id === articleInfo.id)
    if (existingIndex >= 0) {
      // 更新现有记录
      purchasedArticles[existingIndex] = articleInfo
    } else {
      // 添加新记录
      purchasedArticles.unshift(articleInfo)
    }
    
    setItem('purchasedArticles', JSON.stringify(purchasedArticles))
    console.log('已购文章列表已更新:', purchasedArticles)
    
    ElMessage.success('购买成功！已解锁完整内容')
  } catch (error) {
    console.error('购买失败:', error)
    // 拦截器已经显示了错误消息，这里不需要再显示
    // 只有当错误没有被处理时才显示
    if (!error.handled) {
      ElMessage.error(error.message || '购买失败，请重试')
    }
  }
}

// 打赏
const handleReward = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  rewardDialogVisible.value = true
}

const confirmReward = async () => {
  const amount = customAmount.value || rewardAmount.value
  
  if (!amount || amount <= 0) {
    ElMessage.warning('请输入正确的打赏金额')
    return
  }
  
  if (!article.value.authorId) {
    ElMessage.error('无法获取作者信息')
    return
  }
  
  try {
    // 调用打赏API
    await rewardApi({
      toUserId: article.value.authorId,
      amount: amount,
      articleId: route.params.id
    })
    
    ElMessage.success(`打赏 ¥${amount} 成功`)
    rewardDialogVisible.value = false
  } catch (error) {
    console.error('打赏失败:', error)
    // 拦截器已经显示了错误消息，这里不需要再显示
    // 只有当错误没有被处理时才显示
    if (!error.handled) {
      ElMessage.error(error.message || '打赏失败，请重试')
    }
  }
}

// 关注/取消关注作者
const toggleFollowAuthor = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  const authorId = article.value.authorId
  
  try {
    // 调用后端API切换关注状态
    const { toggleFollow } = await import('@/api/interaction')
    const response = await toggleFollow(authorId)
    
    // 更新本地状态
    isFollowingAuthor.value = response.followed
    
    // 显示成功消息
    ElMessage.success(response.message || (response.followed ? `已关注 ${article.value.authorName}` : `已取消关注 ${article.value.authorName}`))
  } catch (error) {
    console.error('关注操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 发表评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  submitting.value = true
  
  try {
    // 调用后端API发表评论
    const token = getItem('token')
    const response = await fetch('http://localhost:8080/api/comments', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        articleId: parseInt(route.params.id),
        content: commentContent.value
      })
    })
    
    const data = await response.json()
    
    if (data.success) {
      ElMessage.success('评论发表成功')
      commentContent.value = ''
      // 重新加载评论列表
      await loadComments()
    } else {
      ElMessage.error(data.message || '评论发表失败')
    }
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('评论发表失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 回复评论
const handleReply = (comment) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 设置要回复的评论
  replyToComment.value = comment
  replyContent.value = ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  submittingReply.value = true
  
  try {
    const token = getItem('token')
    const response = await fetch(`http://localhost:8080/api/comments/${replyToComment.value.id}/reply`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        articleId: parseInt(route.params.id),
        content: replyContent.value
      })
    })
    
    const data = await response.json()
    
    if (data.success) {
      ElMessage.success('回复发表成功')
      replyDialogVisible.value = false
      replyContent.value = ''
      replyToComment.value = null
      // 重新加载评论列表
      await loadComments()
    } else {
      ElMessage.error(data.message || '回复发表失败')
    }
  } catch (error) {
    console.error('发表回复失败:', error)
    ElMessage.error('回复发表失败，请稍后重试')
  } finally {
    submittingReply.value = false
  }
}

// 删除评论
const handleDeleteComment = async (commentId) => {
  try {
    const token = getItem('token')
    const response = await fetch(`http://localhost:8080/api/comments/${commentId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    const data = await response.json()
    
    if (data.success) {
      ElMessage.success('评论删除成功')
      // 重新加载评论列表
      await loadComments()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error('删除评论失败，请稍后重试')
  }
}

// 跳转到个人空间
const goToSpace = (userId) => {
  router.push(`/space/${userId}`)
}

// 滚动到锚点
const scrollToAnchor = (id) => {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

onMounted(() => {
  loadArticle()
  loadComments()
})
</script>


<style scoped>
.article-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
  display: flex;
  gap: 24px;
}

.article-main {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(244, 63, 94, 0.08);
  border: 1px solid #fecdd3;
}

.article-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe4e6;
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #1f2937;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #4b5563;
  font-size: 14px;
}

.author {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: color 0.2s;
}

.author:hover {
  color: #f43f5e;
}

.article-tags {
  margin-top: 14px;
  display: flex;
  gap: 8px;
}

.article-tags .el-tag {
  background: #ffe4e6;
  border-color: #fecdd3;
  color: #e11d48;
}

.pay-notice {
  background: linear-gradient(135deg, #ffe4e6, #fecdd3);
  border: 1px solid #fda4af;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  color: #e11d48;
}

.article-content {
  line-height: 1.9;
  color: #1f2937;
  font-size: 16px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
  overflow-wrap: break-word;
  max-width: 100%;
  min-height: 300px;
  min-width: 800px;
}

/* 图片居中显示 */
.article-content :deep(img) {
  display: block;
  margin: 20px auto;
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.article-content :deep(.locked-content) {
  background: linear-gradient(135deg, #ffe4e6, #fecdd3);
  border: 2px dashed #fda4af;
  border-radius: 12px;
  padding: 28px;
  text-align: center;
  margin: 20px 0;
  color: #e11d48;
}

.article-actions {
  margin-top: 32px;
  padding-top: 20px;
  border-top: 2px solid #ffe4e6;
  display: flex;
  gap: 12px;
}

.comment-section {
  margin-top: 24px;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(244, 63, 94, 0.08);
  border: 1px solid #fecdd3;
}

.section-title {
  margin-bottom: 20px;
  font-size: 18px;
  color: #1f2937;
  font-weight: 600;
}

.comment-input {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.comment-input .el-button {
  align-self: flex-end;
}

.article-sidebar {
  width: 300px;
  flex-shrink: 0;
}

.author-card {
  position: sticky;
  top: 80px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 18px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(244, 63, 94, 0.08);
  border: 1px solid #fecdd3;
  transition: box-shadow 0.3s;
}

.author-card:hover {
  box-shadow: 0 4px 20px rgba(244, 63, 94, 0.12);
}

.author-card h4 {
  margin: 12px 0 8px;
  font-size: 17px;
  color: #1f2937;
}

.author-bio {
  color: #4b5563;
  font-size: 13px;
  margin-bottom: 14px;
}

.reward-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.reward-item {
  width: 80px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fecdd3;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s;
  color: #4b5563;
}

.reward-item:hover {
  border-color: #f43f5e;
  color: #f43f5e;
}

.reward-item.active {
  border-color: #f43f5e;
  color: #fff;
  background: linear-gradient(135deg, #f43f5e, #fb7185);
}

/* 回复对话框样式 */
.reply-to-info {
  margin-bottom: 12px;
  padding: 10px;
  background: #fff5f0;
  border-radius: 8px;
  font-size: 14px;
  color: #636e72;
}

.reply-to-user {
  font-weight: 600;
  color: #f43f5e;
  margin-left: 8px;
}

.reply-original-comment {
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-left: 3px solid #fecdd3;
  border-radius: 4px;
}

.reply-original-comment p {
  margin: 0;
  color: #636e72;
  font-size: 14px;
  line-height: 1.6;
}
</style>
