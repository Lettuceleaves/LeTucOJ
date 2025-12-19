<template>
  <div class="user-profile-container">

    <div class="profile-header-background"
      :style="{ backgroundImage: 'url(' + backgroundImageUrl + ')', height: backgroundHeight + 'px' }">
      <div v-if="loading" class="loading-overlay">加载中...</div>
      <div v-else-if="error" class="error-overlay" @click.stop="fetchUserInfo">
        加载失败，点击重试
      </div>
      <div v-if="isCurrentUserProfile" class="resize-bar" @mousedown.stop="startDrag"></div>
    </div>

    <label v-if="isCurrentUserProfile" class="bg-change-btn-outer"
      :style="{ top: backgroundHeight + 8 + 'px' }">
      <input type="file" accept=".jpg,.jpeg" @change="onSelectBg" style="display: none;">
      👆🏻
    </label>

    <div class="profile-content">
      <div class="avatar-wrapper">
        <div class="avatar-area">
          <img :src="avatarUrl"
            :alt="userInfo.userNickName || userInfo.userName"
            class="avatar-placeholder">
        </div>

        <label v-if="isCurrentUserProfile" class="upload-trigger">
          <input ref="fileInput"
            type="file"
            accept=".jpg,.jpeg"
            @change="onSelectFile">
          👈🏻
        </label>
      </div>

      <div class="info-area">
        <div class="header-and-button">
            <div>
              <h2>{{ userInfo.userNickName || userInfo.userName || '加载中...' }}</h2>
              <p class="user-id">ID: {{ userInfo.userName || '...' }}</p>
            </div>
            <div class="profile-actions"> 
              <button v-if="isCurrentUserProfile" class="logout-btn" @click="logout">
                退出登录
              </button>
              
              <button v-if="isCurrentUserProfile" class="edit-profile-btn" @click="openModal">
                修改资料
              </button>
            </div>
        </div>

        <div class="info-and-search-row"> 
        
            <div class="contact-info">
                <p class="description">
                    <span class="label">描述：</span>
                    {{ userInfo.description || '这个人很懒，什么都没有留下...' }}
                </p>
                <p><span class="label">手机：</span> {{ userInfo.phone || '暂无' }}</p>
                <p><span class="label">邮箱：</span> {{ userInfo.email || '暂无' }}</p>
            </div>
            
            <div class="search-bar-container">
                <input type="text" 
                       v-model="searchUsername" 
                       placeholder="搜索其他用户（输入用户名）"
                       @keyup.enter="searchUser"
                       class="username-search-input">
                <button @click="searchUser" class="search-btn">🔍 搜索</button>
            </div>
            
        </div>

        
      </div>

      <div class="heatmap-section">
          <div class="heatmap-header">
              <h3>
                  活动热力图 ({{ currentHeatmapYear }})
                  <span v-if="heatmapLoading" class="heatmap-loading">加载中...</span>
                  <span v-if="heatmapError" class="heatmap-error" @click="fetchHeatmapData">加载失败，点击重试</span>
              </h3>
              <div class="year-selector">
                  <button @click="changeYear(-1)" :disabled="currentHeatmapYear <= minYear">上一年度</button>
                  <button @click="changeYear(1)" :disabled="currentHeatmapYear >= maxYear">下一年度</button>
              </div>
          </div>
          
          <div v-if="isHeatmapContainerVisible" 
               class="heatmap-placeholder" 
               ref="heatmapChart">
          </div>

          <div v-else class="no-records">
              <p>
                  😔 这懒狗，{{ currentHeatmapYear }}年啥也没写。
              </p>
          </div>
      </div>

      <hr class="section-divider">

        <div v-if="isCurrentUserProfile" class="history-list-placeholder">
          <h3>我的最近提交记录</h3>
        
          <div class="history-list-content">
              <div v-if="historyLoading" class="loading-message">记录加载中...</div>
              
              <div v-else>

                  <ul v-if="sortedRecords.length" class="records">
                      <li v-for="r in sortedRecords" :key="r.submitTime" class="record">
                          <div class="row"><span class="label">题目：</span>{{ r.problemName }}</div>
                          <div class="row"><span class="label">语言：</span>{{ r.language }}</div>
                          <div class="row"><span class="label">结果：</span>{{ r.result }}</div>
                          <div class="row"><span class="label">耗时：</span>{{ r.timeUsed }} ms</div>
                          <div class="row"><span class="label">内存：</span>{{ r.memoryUsed }} KB</div>
                          <div class="row"><span class="label">提交时间：</span>{{ formatTime(r.submitTime) }}</div>

                          <div class="row code-area">
                              <span class="label">代码：</span>
                              <button class="toggle-btn" @click="toggleCode(r)">
                                  {{ r._showCode ? '收起' : '展开' }}
                              </button>
                              <pre v-if="r._showCode" class="code-block">{{ r.code }}</pre>
                          </div>
                      </li>
                  </ul>
                  <p v-else class="no-records">暂无提交记录。</p>
                  <div class="pagination-bar">
                      <button :disabled="start === 0" @click="prevPage">上一页</button>
                      <div class="page-info">
                          第 {{ start / limit + 1 }} 页 / 共 {{ Math.ceil(total / limit) }} 页 (总数: {{ total }})
                      </div>
                      <button :disabled="start + limit >= total" @click="nextPage">下一页</button>
                  </div>
              </div>
          </div>
        </div>
    </div>
  </div>

  <div class="modal-overlay" v-if="isModalVisible">
      <div class="modal-content">
        <h3>编辑个人资料</h3>
        <form @submit.prevent="submitForm">
          
          <div class="form-group">
            <label for="cnname">中文姓名</label>
            <input id="cnname" type="text" v-model="editForm.cnname" required>
          </div>

          <div class="form-group">
            <label for="description">个人描述</label>
            <textarea id="description" v-model="editForm.description" rows="3"></textarea>
          </div>

          <div class="form-group">
            <label for="phone">手机号码</label>
            <input id="phone" type="text" v-model="editForm.phone">
          </div>

          <div class="form-group">
            <label for="email">邮箱</label>
            <input id="email" type="email" v-model="editForm.email">
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="closeModal">取消</button>
            <button type="submit" class="btn-submit">保存</button>
          </div>
        </form>
      </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, nextTick, getCurrentInstance  } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import * as echarts from 'echarts';
import apiService from '../../utils/api.js';

const router = useRouter();
const route = useRoute();

// ===================================
//             配置项
// ===================================

// 默认占位图（如果后端不提供，或者加载失败）
const DEFAULT_AVATAR = 'https://k.sinaimg.cn/n/sinakd20240807ac/775/w397h378/20240807/77f4-c35b3db917df74bca18784f28f95229a.jpg/w700d1q75cms.jpg?by=cms_fixed_width';
// 默认背景：使用一个固定 URL 路径（请确保这个路径是可访问的！）
const DEFAULT_BACKGROUND = 'https://picsum.photos/1200/200';

// ===================================
//             状态和数据
// ===================================

const loading = ref(true);
const error = ref(false);

// 后端模型映射
const userInfo = ref({
  userName: '',
  userNickName: '',
  email: '',
  phone: '',
  description: ''
});

const isModalVisible = ref(false);
const editForm = ref({}); // 用于绑定表单的临时数据

// 图片URL（通常需要单独的接口或逻辑获取）
const avatarUrl = ref(DEFAULT_AVATAR);
const backgroundImageUrl = ref(DEFAULT_BACKGROUND);

const instance = getCurrentInstance()
const ip = instance?.appContext.config.globalProperties.$ip || 'localhost'
const jwtToken = ref(localStorage.getItem('jwt'));

const searchUsername = ref(''); 

// ===================================
//             搜索用户
// ===================================

function searchUser() {
    const username = searchUsername.value.trim();
    if (!username) {
        // 使用自定义消息或模态框代替 alert
        console.warn('请输入要搜索的用户名'); 
        return;
    }

    // 假设您的用户主页路由是 `/user/:username`
    // 如果当前页面已经是目标用户页面，则不跳转
    if (username.toLowerCase() === targetUserName.value.toLowerCase()) {
        // 使用自定义消息或模态框代替 alert
        console.warn(`您已经在用户 ${username} 的主页了。`); 
        return;
    }

    // 执行路由跳转到新的用户主页
    router.push({ 
        // ⭐ 使用路由名称，它能避免 URL 路径被哈希模式影响
        name: 'othersProfile', 
        // ⭐ 使用 query 来传递参数
        query: { 
            pname: username
        } 
    });
}

// ===================================
//             简介信息
// ===================================


function getCurrentUserFromJwt(token) {
    if (!token) return null;

    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const payload = JSON.parse(window.atob(base64));
        return payload.username || payload.sub; 

    } catch (e) {
        console.error("JWT 解析失败", e);
        return null;
    }
}

const targetUserName = ref(null); 

const currentUserLoggedInName = computed(() => {
    return getCurrentUserFromJwt(jwtToken.value); 
});

const isCurrentUserProfile = computed(() => {
    // 1. 实际查询的用户名 (targetUserName.value 已经在 onMounted 中确定)
    const target = targetUserName.value; 
    // 2. 当前登录的用户名
    const loggedIn = currentUserLoggedInName.value;
    
    // 如果 target 或 loggedIn 为空，直接返回 false
    if (!target || !loggedIn) {
        return false;
    }
    
    // 3. 检查当前页面加载的 targetUser 是否与 loggedInUser 相同
    return target.toLowerCase() === loggedIn.toLowerCase();
});


function openModal() {
  editForm.value = { ...userInfo.value };
  isModalVisible.value = true;
}

function closeModal() {
  isModalVisible.value = false;
}

async function submitForm() {
  try {
    const token = localStorage.getItem('jwt');
    const userName = targetUserName.value;
    const requestBody = {
      userName: userName,
      userNickName: editForm.value.userNickName,
      email: editForm.value.email,
      phone: editForm.value.phone,
      description: editForm.value.description
    };

    const res = await apiService.user.updateUserInfo(userName, requestBody);
    const data = res.data;
    
    if (data.code === '0') {
      userInfo.value = { ...editForm.value }; 
      // 使用自定义消息或模态框代替 alert
      console.log('资料更新成功！'); 
    } else {
      throw new Error("API返回错误: " + (data.message || '未知错误'));
    }
    
  } catch (err) {
    console.error('更新用户信息失败:', err);
    // 使用自定义消息或模态框代替 alert
    console.error('更新失败: ' + (err.message || '网络或服务异常')); 
  } finally {
    closeModal();
  }
}

// ===================================
//             数据获取
// ===================================

async function fetchUserInfo() {
  loading.value = true;
  error.value = false;
  
  try {
    const userName = targetUserName.value;
    const res = await apiService.user.getUserInfo(userName);
    const response = res.data;
    
    if (response.code === '0') {
      userInfo.value = response.data;
    } else {
      // 即使请求成功，数据也可能是空的或不符合预期
      throw new Error("API返回数据结构错误");
    }
    
    loading.value = false;
  } catch (err) {
    console.error('获取用户信息失败:', err);
    error.value = true;
    loading.value = false;
  }
}

async function fetchUserAvatar() {
    try {
        const userName = targetUserName.value;
        const res = await apiService.user.getUserAvatar(userName);
        const data = res.data;
        if (data.code === '0') {
          avatarUrl.value = `data:image/jpeg;base64,${data.data}`
        } else {
          console.warn('用户头像未找到，使用默认头像');
        }
    } catch (err) {
        console.error('获取用户头像失败:', err);
    }
}

const fileInput = ref(null)      // 指向隐藏 input

// 选择文件回调
async function onSelectFile(e) {
  const file = e.target.files[0]
  if (!file) return

  // 类型 & 大小校验
  const isJpg = file.type === 'image/jpeg'
  const isLt1M = file.size / 1024 / 1024 < 1

  if (!isJpg) {
    // 使用自定义消息或模态框代替 alert
    console.warn('只能上传 JPG 文件')
    return
  }
  if (!isLt1M) {
    // 使用自定义消息或模态框代替 alert
    console.warn('图片必须小于 1MB')
    return
  }

  // 构造 FormData 并上传
  const formData = new FormData()
  formData.append('file', file)

  try {
    const token = localStorage.getItem('jwt')
    const userName = targetUserName.value;
    const params = new URLSearchParams({
      pname: userName,
    }).toString();
    const res = await apiService.user.updateUserAvatar(userName, file);
    const data = res.data;
    if (data.code === '0') {
      // 上传成功后更新本地头像
      fetchUserAvatar()
      // 使用自定义消息或模态框代替 alert
      console.log('头像更新成功！')
    } else {
      // 使用自定义消息或模态框代替 alert
      console.error('上传失败：' + (data.message || '未知错误'))
    }
  } catch (e) {
    console.error(e)
    // 使用自定义消息或模态框代替 alert
    console.error('网络异常，上传失败')
  }

  // 清空 input，允许重复选同一张图
  fileInput.value.value = ''
}

async function onSelectBg(e) {
  const file = e.target.files[0]
  if (!file) return
  // 使用自定义消息或模态框代替 alert
  if (!file.type.includes('jpeg')) return console.error('只能上传 JPG 背景')
  // 使用自定义消息或模态框代替 alert
  if (file.size > 1_048_576) return console.error('背景图必须 < 1MB')

  try {
    const token = localStorage.getItem('jwt')
    const userName = targetUserName.value;
    const res = await apiService.user.updateUserBackground(userName, file);
    const data = res.data;
    if (data.code === '0') {
      backgroundImageUrl.value = `data:image/jpeg;base64,${data.data}`
      fetchUserBackground()
      // 使用自定义消息或模态框代替 alert
      console.log('背景图更新成功！')
    } else {
      // 使用自定义消息或模态框代替 alert
      console.error('背景上传失败：' + (data.message || '未知错误'))
    }
  } catch (e) {
    console.error(e)
    // 使用自定义消息或模态框代替 alert
    console.error('网络异常')
  }
  e.target.value = '' // 允许重复选同一张
}

// 获取用户背景图
async function fetchUserBackground() {
  try {
    const userName = targetUserName.value;
    const res = await apiService.user.getUserBackground(userName);
    const data = res.data;
    if (data.code === '0' && data.data) {
      backgroundImageUrl.value = `data:image/jpeg;base64,${data.data}`
    } else {
      // 没上传过背景就保持默认图，不弹错
      console.log('未找到用户背景，使用默认')
    }
  } catch (e) {
    console.error('获取背景失败:', e)
  }
}
// 背景高度（默认 200，后续可从后端读）
const backgroundHeight = ref(200)

let dragStartY = 0      // 按下时鼠标 Y
let dragStartHeight = 0 // 按下时背景高度
let dragging = false    // 是否正在拖

// 按下拖动条
function startDrag(e) {
  dragging = true
  dragStartY = e.clientY
  dragStartHeight = backgroundHeight.value
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.body.style.cursor = 'ns-resize'  // 拖动时鼠标样式
}

// 拖动中
function onDrag(e) {
  if (!dragging) return
  const delta = e.clientY - dragStartY      // 向下为正
  let newHeight = dragStartHeight + delta
  // 限制最小 120，最大 500
  newHeight = Math.max(120, Math.min(500, newHeight))
  backgroundHeight.value = newHeight
}

// 松开
function stopDrag() {
  dragging = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.body.style.cursor = ''

  // 可选：把最终高度调接口存后端
  // await saveBgHeight(backgroundHeight.value)
}
// ===================================
//             热力图
// ===================================
const heatmapChart = ref(null);
let myChart = null;
const heatmapLoading = ref(false);
const heatmapError = ref(false);
const heatmapData = ref([]);

const maxYear = new Date().getFullYear();
const minYear = maxYear - 5;
const currentHeatmapYear = ref(maxYear);

// ✅ 【修改 1/3】新增计算属性，用于监听 ECharts 容器的 v-if 状态
const isHeatmapContainerVisible = computed(() => {
    return heatmapData.value.length > 0 || heatmapLoading.value || heatmapError.value;
});

const changeYear = (delta) => {
    const newYear = currentHeatmapYear.value + delta;
    if (newYear >= minYear && newYear <= maxYear) {
        currentHeatmapYear.value = newYear;
    }
};

function transformHeatmapData(data, year) {
    const result = [];
    // 确保 data 是一个对象
    if (!data || typeof data !== 'object') return result;

    for (let month = 1; month <= 12; month++) {
        const monthKey = String(month); 
        const monthData = data[monthKey]; 
        
        if (monthData && Array.isArray(monthData.dailySubmissions)) {
            // dailySubmissions 数组从索引 1 开始，索引 0 是占位符
            // 遍历到 daysInMonth 即可，因为后端已经把数组长度设置为 daysInMonth + 1
            for (let day = 1; day <= monthData.daysInMonth; day++) { 
                const dayValue = monthData.dailySubmissions[day];
                // 即使值为 0 也加入，ECharts 的 visualMap 会将其映射为基础色
                const date = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
                result.push([date, dayValue ?? 0]); // 确保没有值时默认为 0
            }
        }
    }
    return result;
}

// ⚠️ 将 resize 监听器提取为独立函数，以便在 dispose 时可以正确移除
let resizeListener = null;

async function fetchHeatmapData() {
    // 确保用户名已加载
    if (!userInfo.value.userName) {
        renderHeatmap([], currentHeatmapYear.value); 
        return;
    }

    heatmapLoading.value = true;
    heatmapError.value = false;

    try {
        const userName = targetUserName.value;
        const year = currentHeatmapYear.value;

        const res = await apiService.user.getHeatmap(userName, year);
        const response = res.data;

        if (response.code === '0' && response.data) {
            let heatmapJsonData = {};
            try {
                // 1. Base64 解码 (使用 atob)
                const decodedString = atob(response.data);
                
                // 2. JSON 解析。注意：假设后端已修复了键（月份）为字符串的问题。
                heatmapJsonData = JSON.parse(decodedString);
            } catch (e) {
                console.error('热力图数据 Base64 解码或 JSON 解析失败:', e);
                // 抛出错误，进入 catch 块
                throw new Error('热力图数据格式错误'); 
            }
            const transformedData = transformHeatmapData(heatmapJsonData, year);
            heatmapData.value = transformedData;
            
            await nextTick(); 
            renderHeatmap(transformedData, year);
        } else {
            // 如果 data 为空，视为成功加载但无数据
            heatmapData.value = []; 
            renderHeatmap([], year); 
        }
    } catch (err) {
        console.error('获取热力图数据失败:', err);
        heatmapError.value = true;
        heatmapData.value = []; 
        renderHeatmap([], currentHeatmapYear.value); 
    } finally {
        heatmapLoading.value = false;
    }
}


function renderHeatmap(data, year) {
    // 1. 检查 ECharts 容器是否可用
    // heatmapChart 是模板中 ref="heatmapChart" 的引用
    if (!heatmapChart.value) { 
        console.warn('ECharts 容器（ref="heatmapChart"）未加载或引用错误！');
        return;
    }

    // 2. 初始化 ECharts 实例（如果尚未初始化）
    // ⚠️ 【修改 3/3】移除 resize 监听器，并将其逻辑移至 watch(isHeatmapContainerVisible)
    if (!myChart) {
        try {
            myChart = echarts.init(heatmapChart.value);
            // ❌ 移除此处添加 resizeListener 的逻辑，由 watch 统一管理
        } catch (e) {
            console.error("ECharts 初始化失败，请检查容器尺寸：", e);
            return;
        }
    } 
    
    // --- 3. 配置图表选项 ---
    
    // 提交次数的最大值，用于确保 visualMap 覆盖所有数据
    // 如果没有数据，最大值设为 1，确保 0 次提交的颜色能正确显示
    const values = data.map(item => item[1]);
    const maxCommits = values.length > 0 ? Math.max(...values, 1) : 1; 

    const option = {
        tooltip: {
            position: 'top',
            formatter: function (params) {
                // 格式化提示框内容：日期 + 提交次数
                return params.value[0] + ': ' + (params.value[1] ?? 0) + ' 次提交';
            }
        },
        visualMap: {
            // 使用 pieces 进行分段，模拟 GitHub 的颜色梯度
            pieces: [
                { min: 4, label: '4+ 次', color: '#216e39' }, // 深绿色
                { min: 3, max: 3, label: '3 次', color: '#30a14e' },
                { min: 2, max: 2, label: '2 次', color: '#40c463' },
                { min: 1, max: 1, label: '1 次', color: '#9be9a8' },
                { value: 0, label: '0 次', color: '#ebedf0' }  // 无提交颜色
            ],
            orient: 'horizontal',
            left: 'center',
            bottom: 10, 
            text: ['多', '少'], 
            show: true
        },
        calendar: {
            top: 30, 
            left: 25, 
            right: 20, 
            bottom: 50, 
            cellSize: ['auto', 15], // 单元格大小
            range: year, // 设置日历的年份范围
            itemStyle: {
                borderWidth: 0.5,
                borderColor: '#fff' // 单元格之间的白色边框
            },
            dayLabel: {
                nameMap: ['日', '一', '二', '三', '四', '五', '六'],
                color: '#999',
                fontSize: 10,
                margin: 10
            },
            monthLabel: {
                nameMap: 'cn', // 使用中文月份
                color: '#999',
                fontSize: 12,
                margin: 10,
                position: 'start'
            },
            yearLabel: { show: false }
        },
        series: [
            {
                type: 'heatmap',
                coordinateSystem: 'calendar',
                data: data // 传入格式化后的数据
            }
        ]
    };

    // 4. 更新或设置图表选项
    // 使用 setOption(option, true) 清除旧数据并应用新选项
    myChart.setOption(option, true);
}
// ===================================
//             历史记录
// ===================================

const records = ref([])
const start = ref(0)
const limit = 10 // 调整为适合主页的较小分页
const total = ref(0)
const historyLoading = ref(false) // 新增历史记录专用 loading

// 计算属性：排序后的记录
const sortedRecords = computed(() =>
  [...records.value].sort((a, b) => b.submitTime - a.submitTime)
)

// 工具函数：格式化时间（复用主页已有的 formatTime，如果没有则需新增）
const formatTime = (ts) => new Date(ts).toLocaleString()

// 工具函数：代码折叠辅助
const toggleCode = (r) => {
  // 注意：需要确保记录对象 r 包含 _showCode 属性，在 fetchData 中处理
  r._showCode = !r._showCode
}

// 业务函数：获取当前用户的提交历史
async function fetchUserHistory() {
  historyLoading.value = true
  const userName = userInfo.value.userName // 使用已获取的用户名
  
  if (!userName) {
    historyLoading.value = false;
    return;
  }

  // 计算当前页码
  const page = Math.floor(start.value / limit) + 1;

  try {
    const res = await apiService.practice.getUserSubmissions(userName, page, limit);
    const data = res.data;

    if (data.code === "0") {
      records.value = (data.data.records ?? []).map(r => ({ ...r, _showCode: false }));
      total.value = data.data?.amount || 0;
    } else {
      console.error(data.error || '拉取记录失败');
    }
  } catch (e) {
    console.error('网络错误：' + e.message);
  } finally {
    historyLoading.value = false;
  }
}

// 分页回调
const prevPage = () => {
  if (start.value > 0) {
    start.value -= limit
    fetchUserHistory()
  }
}

const nextPage = () => {
  if (start.value + limit < total.value) {
    start.value += limit
    fetchUserHistory()
  }
}

function logout() {
  if (confirm('确定要退出登录吗？')) {
    // 1. 清除 JWT token
    localStorage.removeItem('jwt'); 
    
    // 2. 清除其他可能的本地存储状态 (可选)
    // localStorage.removeItem('userCodeStorage'); 
    
    // 3. 跳转到登录页
    // 假设你的登录路由是 '/login'
    router.push('/login'); 
    // 使用自定义消息或模态框代替 alert
    console.log('已退出登录。');
  }
}

// ------------------------------------


// ===================================
//             生命周期与监听
// ===================================

// ✅ 【修改 2/3】新增 watch 监听容器 v-if 状态，用于手动清理 ECharts 实例
watch(isHeatmapContainerVisible, (isVisible) => {
    if (!isVisible && myChart) {
        // 当 v-if 条件变为 false 时，DOM 元素被销毁。
        // 必须手动 dispose ECharts 实例并移除 resize 监听器。
        
        if (resizeListener) {
            window.removeEventListener('resize', resizeListener);
            resizeListener = null;
        }
        
        try {
            myChart.dispose();
        } catch(e) {
            console.warn('Attempted to dispose already disposed ECharts instance:', e);
        }
        myChart = null; // 重置 myChart，确保下次 v-if 变为 true 时能正确重新初始化
    } else if (isVisible && heatmapChart.value && !myChart) {
        // 当 v-if 变为 true，DOM 挂载完成后，如果 myChart 尚未初始化，则添加 resize 监听器。
        // 由于 nextTick 会在 fetchHeatmapData 内部调用 renderHeatmap，这里主要处理 resize 监听
        // 并在 myChart 实例创建后绑定它。
        
        // **注意：由于 renderHeatmap 内部会在 nextTick 后立即运行，此处的逻辑可以简化。**
        // 确保 myChart 存在后再添加监听器，防止多次添加。
        
        // 重新添加 resize 监听器
        if (!resizeListener) {
             resizeListener = () => {
                if (myChart) {
                    myChart.resize();
                }
            };
            window.addEventListener('resize', resizeListener);
        }
    }
}, { immediate: false });


onMounted(async () => {
    const urlPname = route.query.pname;
    const jwtUser = getCurrentUserFromJwt(jwtToken.value);
    if (urlPname) {
        targetUserName.value = urlPname;
    } else if (jwtUser) {
        targetUserName.value = jwtUser;
    } else {
        console.error("无法确定要加载的用户，请检查登录状态或 URL 参数。");
        return; 
    }
    await fetchUserInfo();
    if (userInfo.value.userName) {
        fetchUserAvatar();
        fetchUserBackground();
        fetchUserHistory();
        fetchHeatmapData();
    }
});

// 监听年份变化，触发数据重新获取
watch(currentHeatmapYear, (newYear, oldYear) => {
    if (newYear !== oldYear) {
        fetchHeatmapData();
    }
});

// 确保在组件完全销毁时，清理所有残留
onUnmounted(() => {
    if (myChart) {
        // 理论上 watch 已经清理了，但这里做最终保险
        if (resizeListener) {
            window.removeEventListener('resize', resizeListener);
        }
        myChart.dispose();
        myChart = null;
    }
    if (resizeListener) { // 再次检查并清理
        window.removeEventListener('resize', resizeListener);
        resizeListener = null;
    }
});
</script>

<style scoped>

.profile-header-background {
  height: 200px;  
  width: 100%;
  position: relative;
  background-color: #a0cfff;  
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  
  /* 当有背景图 URL 时，确保背景色只作为过渡色 */
}

/* 加载状态覆盖层 */
.loading-overlay, .error-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 16px;
    font-weight: bold;
    color: #fff;
}

.loading-overlay {
    background-color: rgba(0, 0, 0, 0.4);
}

.error-overlay {
    background-color: rgba(220, 38, 38, 0.6); /* 红色半透明 */
    cursor: pointer;
}

.avatar-wrapper {
  position: relative;  /* + 号参考此框定位 */
  width: 80px;
  height: 80px;
}

.avatar-area {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;    /* 只裁圆形头像 */
  border: 2px solid #fff;
  box-shadow: 0 0 4px rgba(0,0,0,.2);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* + 号：相对于 avatar-wrapper 定位 */
.upload-trigger {
  position: absolute;
  right: -35px;
  bottom: 25px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #d4e9ff;
  color: #fff;
  font-size: 18px;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
  box-shadow: 0 0 4px rgba(0,0,0,.3);
  transition: background .2s;
}
.upload-trigger:hover {
  background: #67c23a;
}

.upload-trigger input[type=file] {
  display: none;
}

.bg-upload-trigger:hover {
  background: rgba(0, 0, 0, 0.7);
}

.resize-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 8px;
  background: rgba(0,0,0,.1);
  cursor: ns-resize;    /* 上下拖动光标 */
  transition: background .2s;
}
.resize-bar:hover {
  background: rgba(0,0,0,.3);
}

.bg-change-btn {
  position: absolute;
  right: 12px;
  bottom: 12px;           /* 背景图内侧右下 */
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(0,0,0,.5);
  color: #fff;
  font-size: 18px;
  line-height: 32px;
  text-align: center;
  cursor: pointer;
  transition: background .2s;
  z-index: 10;           /* 高于拖动条 */
}
.bg-change-btn:hover {
  background: rgba(0,0,0,.7);
}

.user-profile-container {
  position: relative;    /* 建立包含块 */
}

.bg-change-btn-outer {
  position: absolute;
  right: 12px;
  /* top 由模板动态计算：背景高度 + 8px 间距 */
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgb(232, 248, 255);
  color: #fff;
  font-size: 20px;
  line-height: 36px;
  text-align: center;
  cursor: pointer;
  transition: background .2s;
  z-index: 10;
}
.bg-change-btn-outer:hover {
  background: rgba(215, 234, 255, 0.7);
}

.profile-content {
  position: relative;
  margin: 50px 16px 16px;        /* 上移盖住背景底部 + 左右留边 */
  padding: 24px 20px;
  background: #ffffff;
  border-radius: 16px 16px 0 0;      /* 顶部大圆角 */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  z-index: 1;
}

/* ⭐⭐⭐ 新增样式: 标题和按钮布局 ⭐⭐⭐ */
.header-and-button {
  display: flex;
  justify-content: space-between; /* 标题居左，按钮居右 */
  align-items: center; /* 垂直居中对齐 */
  margin-bottom: 15px; /* 与下方联系信息的间距 */
}

.edit-profile-btn {
  padding: 6px 15px;
  border: 1px solid #1e88e5; /* 蓝色边框 */
  background-color: #ffffff; /* 白色背景 */
  color: #1e88e5; /* 蓝色文字 */
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  flex-shrink: 0; /* 防止按钮被 h2 挤压 */
  margin-left: 20px; /* 与 h2 的间距 */
}

.edit-profile-btn:hover {
  background-color: #e3f2fd; /* 浅蓝背景 */
  color: #0d47a1;
  border-color: #0d47a1;
}

.edit-profile-btn:active {
  background-color: #bbdefb;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 半透明黑色 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000; /* 确保在最上层 */
}

/* 悬浮窗内容区域 */
.modal-content {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px; /* 限制最大宽度 */
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  font-weight: 600;
}

/* 表单组样式 */
.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #555;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input[type="text"],
.form-group input[type="email"],
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box; /* 确保 padding 不增加总宽度 */
  font-size: 14px;
}

.form-group textarea {
    resize: vertical; /* 允许垂直拖动调整大小 */
}

/* 按钮容器 */
.modal-actions {
  display: flex;
  justify-content: flex-end; /* 按钮靠右对齐 */
  margin-top: 25px;
}

/* 提交和取消按钮基础样式 */
.btn-submit, .btn-cancel {
  padding: 8px 18px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 15px;
  transition: background-color 0.2s;
  margin-left: 10px;
}

/* 取消按钮 */
.btn-cancel {
  background-color: #f0f0f0;
  color: #333;
}
.btn-cancel:hover {
  background-color: #e0e0e0;
}

/* 提交按钮 */
.btn-submit {
  background-color: #1e88e5; /* 蓝色 */
  color: #fff;
}
.btn-submit:hover {
  background-color: #1565c0; /* 深蓝色 */
}

.user-id {
    margin-top: -10px; /* 向上微调，减少与 h2 的间距 */
    margin-bottom: 5px; /* 与下方信息的间距 */
    font-size: 14px; /* 小字号 */
    color: #888; /* 灰色 */
    font-weight: 400; /* 避免加粗 */
}

/* 确保 h2 的默认 margin 被覆盖或控制 */
.profile-content h2 {
    margin-top: 0;
    margin-bottom: 5px; /* 调整 h2 底部间距 */
}

/* 修正 .header-and-button，因为它现在包裹了 <button> 和 <div> */
.header-and-button {
    display: flex;
    justify-content: space-between;
    align-items: flex-start; /* 保持左侧内容顶部对齐 */
    margin-bottom: 15px;
}

/* =============== 历史记录新增样式 =============== */

.records {
  list-style: none;
  padding: 0;
  margin: 0;
}
.record {
  background: #f9fafb; /* 浅灰背景，与主页白色区分 */
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.row {
  line-height: 1.5;
  font-size: 0.9em;
  margin-bottom: 4px;
}
.label {
  font-weight: 600;
  color: #4b5563;
  margin-right: 6px;
}

/* 代码区域 */
.code-area {
  margin-top: 8px;
  display: flex;
  align-items: flex-start;
}
.toggle-btn {
  margin-left: 8px;
  font-size: 0.75em;
  cursor: pointer;
  background: #10b981; /* 绿色 */
  color: #fff;
  border: none;
  padding: 3px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}
.toggle-btn:hover {
  background: #059669;
}
.code-block {
  margin: 8px 0 0 0;
  padding: 8px;
  background: #1e293b; /* 深色背景 */
  color: #f8fafc;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre;
  font-family: monospace;
  font-size: 0.8em;
  line-height: 1.4;
  flex-grow: 1; /* 占据剩余空间 */
}

/* Pagination bar styling */
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px 0;
  border-top: 1px solid #eee;
}
.pagination-bar button {
  background: #2563eb;
  color: #fff;
  padding: 6px 12px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 0.85em;
}
.pagination-bar button:hover:not([disabled]) {
  background: #1d4ed8;
}
.pagination-bar button:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}
.page-info {
  font-size: 0.9em;
  color: #6b7280;
  white-space: nowrap;
}
.loading-message, .no-records {
    text-align: center;
    padding: 20px;
    color: #9ca3af;
}

/* 💡 新增：按钮组容器 */
.profile-actions {
  display: flex;
  gap: 10px; /* 按钮之间的间距 */
  flex-shrink: 0;
}

/* 💡 新增：退出登录按钮样式 */
.logout-btn {
  padding: 6px 15px;
  border: 1px solid #dc2626; /* 红色边框 */
  background-color: #ffffff; /* 白色背景 */
  color: #dc2626; /* 红色文字 */
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  flex-shrink: 0;
}

.logout-btn:hover {
  background-color: #fee2e2; /* 浅红背景 */
  color: #991b1b;
  border-color: #991b1b;
}

.logout-btn:active {
  background-color: #fca5a5;
}

/* 调整原来的 edit-profile-btn 的 margin-left，因为它现在在容器内了 */
.edit-profile-btn {
  padding: 6px 15px; 
  border: 1px solid #1e88e5; 
  background-color: #ffffff; 
  color: #1e88e5; 
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  flex-shrink: 0; 
  /* ❗ 移除或改为 0: margin-left: 20px; */ 
}

/* ================ 热力图区域 CSS 优化 ================ */

.heatmap-section {
    margin-bottom: 30px;
    padding: 20px;
    background: #fcfcfc; /* 浅色背景区分 */
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 柔和阴影 */
    border: 1px solid #eee;
}

.heatmap-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 5px;
    border-bottom: 1px solid #f0f0f0; /* 分隔线 */
}

.heatmap-header h3 {
    font-size: 20px; /* 字体适中 */
    font-weight: 600;
    color: #333;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 10px;
}

/* 加载和错误信息样式 */
.heatmap-loading {
    font-size: 14px;
    font-weight: normal;
    color: #1e88e5; /* 蓝色加载中 */
}
.heatmap-error {
    font-size: 14px;
    font-weight: normal;
    color: #ef4444; /* 红色错误 */
    cursor: pointer;
}

/* 年度选择按钮样式 */
.year-selector button {
    background: #fff;
    border: 1px solid #ccc;
    color: #555;
    padding: 5px 12px;
    border-radius: 6px; /* 略圆 */
    cursor: pointer;
    margin-left: 8px;
    transition: background 0.2s, border-color 0.2s;
    font-size: 14px;
}
.year-selector button:hover:not([disabled]) {
    background: #f0f0f0;
    border-color: #999;
}
.year-selector button:disabled {
    cursor: not-allowed;
    opacity: 0.5;
}

/* ECharts 容器 - 关键是确保有尺寸 */
.heatmap-placeholder {
    width: 100%;
    min-height: 250px; /* 确保图表有足够的高度，防止不渲染 */
    margin-top: 15px;
}

.no-records {
    text-align: center;
    padding: 30px;
    color: #9ca3af;
    font-size: 16px;
}

/* 搜索栏容器 */
.search-bar-container {
    flex-grow: 1;
    display: flex;
    margin: 20px 0 30px;
    padding: 0 20px;
    gap: 10px;
}

/* 搜索输入框 */
.username-search-input {
    flex-grow: 1;
    padding: 10px 15px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 1em;
    transition: border-color 0.3s;
}

.username-search-input:focus {
    border-color: #409eff; /* 蓝色焦点边框 */
    outline: none;
}

/* 搜索按钮 */
.search-btn {
    padding: 10px 15px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 1em;
    transition: background-color 0.3s;
}

.search-btn:hover {
    background-color: #66b1ff;
}
.info-and-search-row {
    display: flex;
    justify-content: space-between; /* 元素左右两端对齐 */
    align-items: flex-start; /* 顶部对齐 */
    margin-bottom: 25px; 
    flex-wrap: wrap; /* 允许小屏幕时自动换行 */
}

/* 左侧：个人简介区域 */
.contact-info {
    flex: 1 1 60%; /* 占据约 60% 宽度 */
    min-width: 280px; 
    padding-right: 20px; 
    margin-bottom: 10px;
}
</style>