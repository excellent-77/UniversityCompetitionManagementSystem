<template>
  <div class="hello-world">
    <h1>{{ msg }}</h1>
    <p>欢迎使用高校竞赛管理平台！</p>
    <div class="features">
      <div class="feature-item">
        <el-icon :size="32"><Trophy /></el-icon>
        <h3>竞赛管理</h3>
        <p>发布、报名、评审一站式管理</p>
      </div>
      <div class="feature-item">
        <el-icon :size="32"><UserFilled /></el-icon>
        <h3>智能组队</h3>
        <p>基于技能标签智能推荐队友</p>
      </div>
      <div class="feature-item">
        <el-icon :size="32"><Guide /></el-icon>
        <h3>师生匹配</h3>
        <p>智能匹配指导教师</p>
      </div>
      <div class="feature-item">
        <el-icon :size="32"><DataAnalysis /></el-icon>
        <h3>数据分析</h3>
        <p>竞赛数据可视化分析</p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  msg: {
    type: String,
    default: '高校竞赛管理平台'
  }
})
</script>

<style scoped lang="scss">
.hello-world {
  text-align: center;
  padding: 40px;
  
  h1 {
    font-size: 32px;
    color: #1e3c72;
    margin-bottom: 16px;
  }
  
  p {
    font-size: 16px;
    color: #666;
    margin-bottom: 40px;
  }
  
  .features {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
    margin-top: 40px;
    
    .feature-item {
      padding: 24px;
      background: #f8f9fa;
      border-radius: 12px;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
      }
      
      .el-icon {
        color: #1e3c72;
        margin-bottom: 12px;
      }
      
      h3 {
        font-size: 18px;
        margin-bottom: 8px;
        color: #333;
      }
      
      p {
        font-size: 14px;
        color: #999;
        margin-bottom: 0;
      }
    }
  }
}
</style>