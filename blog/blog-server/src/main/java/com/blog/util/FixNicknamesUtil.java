package com.blog.util;

import com.blog.entity.User;
import com.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixNicknamesUtil implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // 只在启动时执行一次
        if (args.length > 0 && "fix-nicknames".equals(args[0])) {
            fixNicknames();
        }
    }

    public void fixNicknames() {
        log.info("开始修复用户昵称和头像...");

        updateUser(3L, "胡雪岩", "红顶商人，徽商代表，一代商圣。经商之道，在于诚信为本，以义取利。", 
                   "https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan");
        updateUser(4L, "左宗棠", "晚清名臣，收复新疆，创办福建船政局。",
                   "https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang");
        updateUser(5L, "盛宣怀", "洋务运动代表，创办轮船招商局、北洋大学堂。",
                   "https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai");
        updateUser(6L, "张謇", "状元实业家，创办大生纱厂，实业救国的先驱。",
                   "https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian");
        updateUser(7L, "李鸿章", "晚清重臣，洋务运动领袖，外交家。",
                   "https://api.dicebear.com/7.x/avataaars/svg?seed=lihongzhang");

        log.info("用户昵称和头像修复完成！");
        
        // 验证
        for (long id = 3; id <= 7; id++) {
            Optional<User> user = userRepository.findById(id);
            user.ifPresent(u -> log.info("ID: {}, 昵称: {}, 头像: {}", u.getId(), u.getNickname(), u.getAvatar()));
        }
    }

    private void updateUser(Long id, String nickname, String bio, String avatar) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setNickname(nickname);
            user.setBio(bio);
            user.setAvatar(avatar);
            userRepository.save(user);
            log.info("更新用户 ID={}: {} - {}", id, nickname, avatar);
        }
    }
}
