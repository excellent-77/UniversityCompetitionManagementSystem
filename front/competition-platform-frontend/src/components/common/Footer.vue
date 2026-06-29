<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-links">
        <a href="#" @click.prevent>关于我们</a>
        <a href="#" @click.prevent>联系方式</a>
        <a href="#" @click.prevent>帮助中心</a>
        <a href="#" @click.prevent>隐私政策</a>
      </div>
      <div class="footer-copyright">
        <p>© 2026 高校竞赛管理平台 | 让竞赛组织更高效，让团队协作更轻松</p>
        <p>南通理工学院 软件工程专业 毕业设计</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped lang="scss">
.app-footer {
  background: #1e3c72;
  color: #fff;
  padding: 32px 24px;
  margin-top: auto;
  
  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    text-align: center;
    
    .footer-links {
      display: flex;
      justify-content: center;
      gap: 32px;
      margin-bottom: 20px;
      
      a {
        color: rgba(255, 255, 255, 0.7);
        text-decoration: none;
        font-size: 14px;
        transition: color 0.2s;
        
        &:hover {
          color: #fff;
        }
      }
    }
    
    .footer-copyright {
      p {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.5);
        margin: 4px 0;
      }
    }
  }
}
</style>