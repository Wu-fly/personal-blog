/**
 * 博客系统性能测试脚本
 * 测试主要API接口的平均响应时间
 */

const https = require('https');
const http = require('http');

// 配置
const BASE_URL = 'http://localhost:8080';
const TEST_ITERATIONS = 10; // 每个接口测试10次

// 测试的API接口列表
const API_ENDPOINTS = [
  {
    name: '首页文章列表',
    method: 'GET',
    path: '/api/articles?page=0&size=10'
  },
  {
    name: '文章详情',
    method: 'GET',
    path: '/api/articles/1'
  },
  {
    name: '用户空间',
    method: 'GET',
    path: '/api/users/15/space'
  },
  {
    name: '文章评论',
    method: 'GET',
    path: '/api/comments/article/1'
  },
  {
    name: '标签列表',
    method: 'GET',
    path: '/api/tags'
  },
  {
    name: '分类列表',
    method: 'GET',
    path: '/api/categories'
  }
];

// 发送HTTP请求并测量响应时间
function testEndpoint(endpoint) {
  return new Promise((resolve, reject) => {
    const url = new URL(endpoint.path, BASE_URL);
    const startTime = Date.now();
    
    const req = http.request({
      hostname: url.hostname,
      port: url.port,
      path: url.pathname + url.search,
      method: endpoint.method,
      headers: {
        'Content-Type': 'application/json'
      }
    }, (res) => {
      let data = '';
      
      res.on('data', (chunk) => {
        data += chunk;
      });
      
      res.on('end', () => {
        const endTime = Date.now();
        const responseTime = endTime - startTime;
        
        resolve({
          statusCode: res.statusCode,
          responseTime: responseTime,
          success: res.statusCode >= 200 && res.statusCode < 300
        });
      });
    });
    
    req.on('error', (error) => {
      const endTime = Date.now();
      const responseTime = endTime - startTime;
      
      resolve({
        statusCode: 0,
        responseTime: responseTime,
        success: false,
        error: error.message
      });
    });
    
    req.setTimeout(10000, () => {
      req.destroy();
      reject(new Error('Request timeout'));
    });
    
    req.end();
  });
}

// 运行单个接口的多次测试
async function runEndpointTests(endpoint) {
  console.log(`\n测试接口: ${endpoint.name}`);
  console.log(`路径: ${endpoint.method} ${endpoint.path}`);
  console.log('='.repeat(60));
  
  const results = [];
  
  for (let i = 0; i < TEST_ITERATIONS; i++) {
    try {
      const result = await testEndpoint(endpoint);
      results.push(result);
      
      const status = result.success ? '✓' : '✗';
      console.log(`  第 ${i + 1} 次: ${status} ${result.responseTime}ms (状态码: ${result.statusCode})`);
      
      // 避免请求过快
      await new Promise(resolve => setTimeout(resolve, 100));
    } catch (error) {
      console.log(`  第 ${i + 1} 次: ✗ 失败 - ${error.message}`);
    }
  }
  
  // 计算统计数据
  const successResults = results.filter(r => r.success);
  const responseTimes = successResults.map(r => r.responseTime);
  
  if (responseTimes.length > 0) {
    const avgTime = responseTimes.reduce((a, b) => a + b, 0) / responseTimes.length;
    const minTime = Math.min(...responseTimes);
    const maxTime = Math.max(...responseTimes);
    const successRate = (successResults.length / results.length * 100).toFixed(2);
    
    console.log('\n统计结果:');
    console.log(`  成功率: ${successRate}% (${successResults.length}/${results.length})`);
    console.log(`  平均响应时间: ${avgTime.toFixed(2)}ms`);
    console.log(`  最快响应时间: ${minTime}ms`);
    console.log(`  最慢响应时间: ${maxTime}ms`);
    
    return {
      endpoint: endpoint.name,
      avgTime: avgTime,
      minTime: minTime,
      maxTime: maxTime,
      successRate: parseFloat(successRate),
      totalTests: results.length,
      successTests: successResults.length
    };
  } else {
    console.log('\n统计结果: 所有请求均失败');
    return {
      endpoint: endpoint.name,
      avgTime: 0,
      minTime: 0,
      maxTime: 0,
      successRate: 0,
      totalTests: results.length,
      successTests: 0
    };
  }
}

// 主测试函数
async function runPerformanceTests() {
  console.log('╔═══════════════════════════════════════════════════════════╗');
  console.log('║          博客系统性能测试 - API响应时间测试              ║');
  console.log('╚═══════════════════════════════════════════════════════════╝');
  console.log(`\n测试配置:`);
  console.log(`  基础URL: ${BASE_URL}`);
  console.log(`  每个接口测试次数: ${TEST_ITERATIONS}`);
  console.log(`  测试接口数量: ${API_ENDPOINTS.length}`);
  
  const allResults = [];
  
  for (const endpoint of API_ENDPOINTS) {
    const result = await runEndpointTests(endpoint);
    allResults.push(result);
  }
  
  // 打印总体统计
  console.log('\n\n');
  console.log('╔═══════════════════════════════════════════════════════════╗');
  console.log('║                      总体测试结果                         ║');
  console.log('╚═══════════════════════════════════════════════════════════╝');
  console.log('\n');
  
  console.log('接口名称                    平均响应时间    最小    最大    成功率');
  console.log('-'.repeat(70));
  
  allResults.forEach(result => {
    const name = result.endpoint.padEnd(25);
    const avg = `${result.avgTime.toFixed(2)}ms`.padEnd(15);
    const min = `${result.minTime}ms`.padEnd(7);
    const max = `${result.maxTime}ms`.padEnd(7);
    const rate = `${result.successRate}%`;
    
    console.log(`${name} ${avg} ${min} ${max} ${rate}`);
  });
  
  // 计算总体平均响应时间
  const successfulResults = allResults.filter(r => r.successRate > 0);
  if (successfulResults.length > 0) {
    const overallAvg = successfulResults.reduce((sum, r) => sum + r.avgTime, 0) / successfulResults.length;
    const overallSuccessRate = allResults.reduce((sum, r) => sum + r.successRate, 0) / allResults.length;
    
    console.log('\n' + '='.repeat(70));
    console.log(`\n总体平均响应时间: ${overallAvg.toFixed(2)}ms`);
    console.log(`总体成功率: ${overallSuccessRate.toFixed(2)}%`);
    
    // 性能评估
    console.log('\n性能评估:');
    if (overallAvg < 100) {
      console.log('  ✓ 优秀 - 响应时间小于100ms');
    } else if (overallAvg < 300) {
      console.log('  ✓ 良好 - 响应时间在100-300ms之间');
    } else if (overallAvg < 1000) {
      console.log('  ⚠ 一般 - 响应时间在300-1000ms之间,建议优化');
    } else {
      console.log('  ✗ 较差 - 响应时间超过1000ms,需要优化');
    }
  }
  
  console.log('\n测试完成!\n');
}

// 运行测试
runPerformanceTests().catch(error => {
  console.error('测试过程中发生错误:', error);
  process.exit(1);
});
