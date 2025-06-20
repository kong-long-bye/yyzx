<template>
  <div class="admin-container">
    <!-- 顶部用户信息栏 -->
    <div class="user-bar">
      <div class="user-info">
        <span>欢迎，{{ currentUser?.realName || currentUser?.username }}</span>
        <span class="role-badge">{{ getRoleName(currentUser?.roleId) }}</span>
      </div>
      <button @click="logout" class="logout-btn">退出登录</button>
    </div>

    <!-- 原有的管理界面内容 -->
    <div class="container">
      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="logo">
          <h2>东软颐养中心</h2>
        </div>
        <div class="nav-menu">
          <div class="menu-item">
            <div class="menu-header active" @click="toggleMenu">
              <span class="menu-icon">🛏️</span>
              <span class="menu-text">床位管理</span>
              <span class="menu-arrow expanded">▼</span>
            </div>
            <div class="submenu show">
              <div class="submenu-item active" @click="switchTab('bed-overview')">床位管理示意图</div>
              <div class="submenu-item" @click="switchTab('bed-management')">床位管理</div>
            </div>
          </div>

          <div class="menu-item">
            <div class="menu-header" @click="toggleMenu">
              <span class="menu-icon">👥</span>
              <span class="menu-text">客户管理</span>
              <span class="menu-arrow">▼</span>
            </div>
            <div class="submenu">
              <div class="submenu-item" @click="switchTab('customer-checkin')">入住登记</div>
              <div class="submenu-item" @click="switchTab('customer-outing')">外出登记</div>
              <div class="submenu-item" @click="switchTab('customer-checkout')">退住登记</div>
            </div>
          </div>

          <div class="menu-item">
            <div class="menu-header" @click="toggleMenu">
              <span class="menu-icon">🏥</span>
              <span class="menu-text">护理管理</span>
              <span class="menu-arrow">▼</span>
            </div>
            <div class="submenu">
              <div class="submenu-item" @click="switchTab('nursing-level')">护理级别</div>
              <div class="submenu-item" @click="switchTab('nursing-project')">护理项目</div>
              <div class="submenu-item" @click="switchTab('nursing-needs')">客户护理需求</div>
              <div class="submenu-item" @click="switchTab('nursing-records')">护理记录</div>
            </div>
          </div>

          <div class="menu-item">
            <div class="menu-header" @click="toggleMenu">
              <span class="menu-icon">💊</span>
              <span class="menu-text">健康管家</span>
              <span class="menu-arrow">▼</span>
            </div>
            <div class="submenu">
              <div class="submenu-item" @click="switchTab('service-assignment')">设置服务对象</div>
              <div class="submenu-item" @click="switchTab('service-focus')">服务关注</div>
            </div>
          </div>

          <div class="menu-item">
            <div class="menu-header" @click="toggleMenu">
              <span class="menu-icon">👤</span>
              <span class="menu-text">用户管理</span>
              <span class="menu-arrow">▼</span>
            </div>
            <div class="submenu">
              <div class="submenu-item" @click="switchTab('user-management')">基础数据维护</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <div class="header">
          <h1 id="page-title">床位管理</h1>
        </div>

        <div class="search-bar">
          <input type="text" class="search-input" placeholder="请输入搜索关键词...">
        </div>

        <div class="content-area">
          <!-- 床位管理示意图 -->
          <div id="bed-overview" class="tab-content active">
            <div class="bed-overview">
              <h2 class="section-title">床位使用统计</h2>
              <div class="stats-grid">
                <div class="stat-card">
                  <div class="stat-number">120</div>
                  <div class="stat-label">总床位数</div>
                </div>
                <div class="stat-card">
                  <div class="stat-number">98</div>
                  <div class="stat-label">已占用</div>
                </div>
                <div class="stat-card">
                  <div class="stat-number">15</div>
                  <div class="stat-label">空闲床位</div>
                </div>
                <div class="stat-card">
                  <div class="stat-number">7</div>
                  <div class="stat-label">维修中</div>
                </div>
              </div>

              <h2 class="section-title">床位分布图</h2>

              <!-- 一楼 -->
              <div class="floor-section">
                <div class="floor-header">一楼 (A区)</div>
                <div class="room-grid">
                  <div class="room-card">
                    <div class="room-header">
                      <span class="room-number">A101</span>
                      <span class="room-type">双人间</span>
                    </div>
                    <div class="beds-container">
                      <div class="bed-item bed-occupied">A101-1<br>张三</div>
                      <div class="bed-item bed-available">A101-2<br>空闲</div>
                    </div>
                  </div>
                  <div class="room-card">
                    <div class="room-header">
                      <span class="room-number">A102</span>
                      <span class="room-type">双人间</span>
                    </div>
                    <div class="beds-container">
                      <div class="bed-item bed-occupied">A102-1<br>李四</div>
                      <div class="bed-item bed-occupied">A102-2<br>王五</div>
                    </div>
                  </div>
                  <div class="room-card">
                    <div class="room-header">
                      <span class="room-number">A103</span>
                      <span class="room-type">单人间</span>
                    </div>
                    <div class="beds-container">
                      <div class="bed-item bed-maintenance">A103-1<br>维修中</div>
                    </div>
                  </div>
                  <div class="room-card">
                    <div class="room-header">
                      <span class="room-number">A104</span>
                      <span class="room-type">双人间</span>
                    </div>
                    <div class="beds-container">
                      <div class="bed-item bed-occupied">A104-1<br>赵六</div>
                      <div class="bed-item bed-reserved">A104-2<br>预留</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 二楼 -->
              <div class="floor-section">
                <div class="floor-header">二楼 (B区)</div>
                <div class="room-grid">
                  <div class="room-card">
                    <div class="room-header">
                      <span class="room-number">B201</span>
                      <span class="room-type">双人间</span>
                    </div>
                    <div class="beds-container">
                      <div class="bed-item bed-occupied">B201-1<br>钱七</div>
                      <div class="bed-item bed-occupied">B201-2<br>孙八</div>
                    </div>
                  </div>
                  <div class="room-card">
                    <div class="room-header">
                      <span class="room-number">B202</span>
                      <span class="room-type">三人间</span>
                    </div>
                    <div class="beds-container">
                      <div class="bed-item bed-occupied">B202-1<br>周九</div>
                      <div class="bed-item bed-available">B202-2<br>空闲</div>
                      <div class="bed-item bed-available">B202-3<br>空闲</div>
                    </div>
                  </div>
                  <div class="room-card">
                    <div class="room-header">
                      <span class="room-number">B203</span>
                      <span class="room-type">双人间</span>
                    </div>
                    <div class="beds-container">
                      <div class="bed-item bed-maintenance">B203-1<br>维修中</div>
                      <div class="bed-item bed-available">B203-2<br>空闲</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 其他内容省略，此处只展示核心功能 -->
          <div id="bed-management" class="tab-content">
            <h2 class="section-title">床位管理功能开发中...</h2>
          </div>

          <div id="customer-checkin" class="tab-content">
            <h2 class="section-title">客户入住登记功能开发中...</h2>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { User, UserRole } from '@/types'

// 组合式API设置
const router = useRouter()
const currentUser = ref<User | null>(null)

onMounted(() => {
  // 获取当前登录用户信息
  const userStr = localStorage.getItem('user')
  if (userStr) {
    currentUser.value = JSON.parse(userStr) as User
  } else {
    // 如果没有用户信息，跳转到登录页
    router.push('/login')
  }
})

const getRoleName = (roleId?: number): string => {
  const roleMap: Record<number, string> = {
    1: '系统管理员',
    2: '健康管家',
    3: '医护人员'
  }
  return roleMap[roleId || 0] || '未知角色'
}

const logout = (): void => {
  if (confirm('确认退出登录吗？')) {
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    router.push('/login')
  }
}

const toggleMenu = (event: Event): void => {
  const element = event.currentTarget as HTMLElement
  const submenu = element.nextElementSibling as HTMLElement
  const arrow = element.querySelector('.menu-arrow') as HTMLElement

  // 先关闭其他菜单
  document.querySelectorAll('.menu-header').forEach(header => {
    if (header !== element) {
      header.classList.remove('active')
      const otherArrow = header.querySelector('.menu-arrow') as HTMLElement
      const otherSubmenu = header.nextElementSibling as HTMLElement
      if (otherArrow) otherArrow.classList.remove('expanded')
      if (otherSubmenu && otherSubmenu.classList.contains('submenu')) {
        otherSubmenu.classList.remove('show')
      }
    }
  })

  // 切换当前菜单
  if (submenu && submenu.classList.contains('submenu')) {
    submenu.classList.toggle('show')
    arrow.classList.toggle('expanded')
    element.classList.toggle('active')
  }
}

const switchTab = (tabId: string): void => {
  // 隐藏所有内容
  document.querySelectorAll('.tab-content').forEach(content => {
    content.classList.remove('active')
  })

  // 取消所有子菜单选中状态
  document.querySelectorAll('.submenu-item').forEach(item => {
    item.classList.remove('active')
  })

  // 显示对应内容
  const targetTab = document.getElementById(tabId)
  if (targetTab) {
    targetTab.classList.add('active')
  }

  // 设置对应菜单项为选中状态
  if (event?.target) {
    (event.target as HTMLElement).classList.add('active')
  }

  // 更新页面标题
  const titles: Record<string, string> = {
    'bed-overview': '床位管理 - 床位分布图',
    'bed-management': '床位管理 - 床位管理',
    'customer-checkin': '客户管理 - 入住登记',
    'customer-outing': '客户管理 - 外出登记',
    'customer-checkout': '客户管理 - 退住登记',
    'nursing-level': '护理管理 - 护理级别',
    'nursing-project': '护理管理 - 护理项目',
    'nursing-needs': '护理管理 - 客户护理需求',
    'nursing-records': '护理管理 - 护理记录',
    'service-assignment': '健康管家 - 设置服务对象',
    'service-focus': '健康管家 - 服务关注',
    'user-management': '用户管理 - 基础数据维护'
  }

  const titleElement = document.getElementById('page-title')
  if (titleElement) {
    titleElement.textContent = titles[tabId] || '东软颐养中心'
  }
}
</script>

<style scoped>
/* 用户栏样式 */
.user-bar {
  background: #2c3e50;
  color: white;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.role-badge {
  background: #3498db;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.logout-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.logout-btn:hover {
  background: #c0392b;
}

/* 原有样式 */
.admin-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Microsoft YaHei', sans-serif;
  background-color: #f5f5f5;
  color: #333;
}

.container {
  display: flex;
  height: calc(100vh - 50px);
}

/* 侧边栏样式 */
.sidebar {
  width: 250px;
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  color: white;
  overflow-y: auto;
  box-shadow: 2px 0 10px rgba(0,0,0,0.1);
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #34495e;
  background-color: #2c3e50;
}

.logo h2 {
  color: #ecf0f1;
  font-size: 18px;
  font-weight: 600;
}

.nav-menu {
  padding: 0;
}

.menu-item {
  border-bottom: 1px solid #34495e;
}

.menu-header {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: transparent;
}

.menu-header:hover {
  background-color: #34495e;
}

.menu-header.active {
  background-color: #e74c3c;
  color: white;
}

.menu-icon {
  margin-right: 10px;
  font-size: 16px;
  width: 20px;
}

.menu-text {
  flex: 1;
  font-size: 14px;
}

.menu-arrow {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.menu-arrow.expanded {
  transform: rotate(180deg);
}

.submenu {
  background-color: #2980b9;
  display: none;
  animation: slideDown 0.3s ease;
}

.submenu.show {
  display: block;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 200px;
  }
}

.submenu-item {
  padding: 12px 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
  font-size: 13px;
}

.submenu-item:hover {
  background-color: #3498db;
  border-left-color: #ecf0f1;
}

.submenu-item.active {
  background-color: #3498db;
  border-left-color: #f1c40f;
  font-weight: 600;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: white;
}

.header {
  background: linear-gradient(90deg, #3498db 0%, #2980b9 100%);
  color: white;
  padding: 15px 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header h1 {
  font-size: 24px;
  font-weight: 500;
}

.search-bar {
  margin: 20px 30px;
}

.search-input {
  width: 300px;
  padding: 10px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 25px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  border-color: #3498db;
}

.content-area {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.tab-content {
  display: none;
}

.tab-content.active {
  display: block;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.section-title {
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 20px;
  border-left: 4px solid #3498db;
  padding-left: 15px;
}

/* 床位管理样式 */
.bed-overview {
  background: white;
  border-radius: 10px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: linear-gradient(45deg, #3498db, #2980b9);
  color: white;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
}

.floor-section {
  margin-bottom: 30px;
}

.floor-header {
  background: #ecf0f1;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 15px;
  font-weight: bold;
  color: #2c3e50;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.room-card {
  border: 2px solid #ecf0f1;
  border-radius: 8px;
  padding: 15px;
  background: white;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.room-number {
  font-weight: bold;
  color: #2c3e50;
}

.room-type {
  font-size: 12px;
  color: #7f8c8d;
}

.beds-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.bed-item {
  padding: 10px;
  border-radius: 5px;
  text-align: center;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.bed-available {
  background: #d5f4e6;
  color: #27ae60;
  border: 1px solid #27ae60;
}

.bed-occupied {
  background: #fadbd8;
  color: #e74c3c;
  border: 1px solid #e74c3c;
}

.bed-maintenance {
  background: #fdeaa7;
  color: #f39c12;
  border: 1px solid #f39c12;
}

.bed-reserved {
  background: #d6eaf8;
  color: #3498db;
  border: 1px solid #3498db;
}
</style>