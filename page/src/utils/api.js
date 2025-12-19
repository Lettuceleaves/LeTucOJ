import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:7777',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 从localStorage获取JWT令牌
    const token = localStorage.getItem('jwt');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 解析JWT令牌获取用户信息
const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
    // eslint-disable-next-line no-unused-vars
  } catch (e) {
    return {};
  }
};

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 如果返回的token，保存到localStorage
    if (response.data && response.data.token) {
      localStorage.setItem('jwt', response.data.token);

      // 解析JWT令牌获取角色信息
      const payload = parseJwt(response.data.token);
      if (payload.role) {
        // 获取当前userInfo
        const userInfoStr = localStorage.getItem('userInfo');
        if (userInfoStr) {
          const userInfo = JSON.parse(userInfoStr);
          // 更新角色信息
          userInfo.role = payload.role;
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
        }
      }
    }
    return response;
  },
  (error) => {
    // 处理401未授权错误，但不自动跳转到登录页面，允许页面显示公开信息
    if (error.response && error.response.status === 401) {
      console.warn('API请求未授权，请检查登录状态');
    }
    return Promise.reject(error);
  }
);

// API接口封装
const apiService = {
  // 用户模块
  user: {
    // 登录
    login(userName, password) {
      return api.post('/user/login', { userName, password });
    },
    // 注册
    register(userName, password, userNickName) {
      return api.post('/user/register', { userName: userName, password, userNickName });
    },
    // 退出登录
    logout(userName) {
      return api.post(`/user/logout?user_name=${userName}`);
    },
    // 获取用户信息
    getUserInfo(userName) {
      return api.get('/user/info', { params: { user_name: userName } });
    },
    // 更新用户信息
    updateUserInfo(userName, userInfo) {
      return api.put(`/user/info?user_name=${userName}`, userInfo);
    },
    // 获取用户排名
    getRank() {
      return api.get('/user/rank');
    },
    // 获取用户热力图
    getHeatmap(userName, year) {
      return api.get('/user/heatmap', { params: { user_name: userName, year } });
    },
    // 获取用户头像
    getUserAvatar(userName) {
      return api.get('/user/headPortrait', { params: { user_name: userName } });
    },
    // 更新用户头像
    updateUserAvatar(userName, file) {
      const formData = new FormData();
      formData.append('file', file);
      return api.put(`/user/headPortrait?user_name=${userName}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
    },
    // 获取用户背景图
    getUserBackground(userName) {
      return api.get('/user/background', { params: { user_name: userName } });
    },
    // 更新用户背景图
    updateUserBackground(userName, file) {
      const formData = new FormData();
      formData.append('file', file);
      return api.put(`/user/background?user_name=${userName}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
    },
    // 激活用户
    activateUser(userName) {
      return api.post(`/user/activate?user_name=${userName}`);
    },
    // 停用用户
    deactivateUser(userName) {
      return api.post(`/user/deactivate?user_name=${userName}`);
    },
    // 设置管理员
    setAdmin(userName) {
      return api.post(`/user/setAdmin?user_name=${userName}`);
    },
    // 取消管理员
    cancelAdmin(userName) {
      return api.post(`/user/cancelAdmin?user_name=${userName}`);
    }
  },

  // 练习模块
  practice: {
    // 获取题目列表
    getProblemList(page, pageSize, order, onlyPublic = false) {
      const start = (page - 1) * pageSize;
      return api.get('/practice/list', { params: { start, limit: pageSize, order, onlyPublic } });
    },
    // 搜索题目列表
    searchProblems(keyword, page, pageSize, order, onlyPublic = false) {
      const start = (page - 1) * pageSize;
      return api.get('/practice/list_search', { params: { like: keyword, start, limit: pageSize, order, onlyPublic } });
    },
    // 获取题目详情
    getProblem(problemName, role) {
      return api.get('/practice/problem', { params: { problem_name: problemName, role } });
    },
    // 创建题目
    createProblem(problem) {
      return api.post('/practice/problem', problem);
    },
    // 更新题目
    updateProblem(problemName, problem) {
      return api.put(`/practice/problem?problem_name=${problemName}`, problem);
    },
    // 删除题目
    deleteProblem(problemName) {
      return api.delete(`/practice/problem?problem_name=${problemName}`);
    },
    // 提交代码
    submitCode(problemName, lang, code) {
      return api.post(`/practice/submit?language=${lang}&problem_name=${problemName}`, code, {
        headers: {
          'Content-Type': 'text/plain'
        }
      });
    },
    // 获取提交记录
    getSubmissions(page = 1, size = 10) {
      const start = (page - 1) * size;
      return api.get('/practice/list_record', { params: { start, limit: size } });
    },
    // 获取个人提交记录
    getSelfSubmissions(page = 1, size = 10) {
      const start = (page - 1) * size;
      return api.get('/practice/list_record/self', { params: { start, limit: size } });
    },
    // 获取指定用户提交记录
    getUserSubmissions(userName, page = 1, size = 10) {
      const start = (page - 1) * size;
      return api.get('/practice/list_record/any', { params: { user_name: userName, start, limit: size } });
    },
    // 获取测试任务
    getTestTask(traceId) {
      return api.get('/practice/testTask', { params: { trace_id: traceId } });
    }
  },

  // 比赛模块
  contest: {
    // 获取比赛列表
    getContestList() {
      return api.get('/contest/contests');
    },
    // 获取比赛详情
    getContest(contestName) {
      return api.get('/contest/contest', { params: { contest_name: contestName } });
    },
    // 创建比赛
    createContest(contest) {
      return api.post('/contest/contest', contest);
    },
    // 更新比赛
    updateContest(contestName, contest) {
      return api.put(`/contest/contest?contest_name=${contestName}`, contest);
    },
    // 获取比赛题目列表
    getContestProblems(contestName) {
      return api.get('/contest/problems', { params: { contest_name: contestName } });
    },
    // 获取比赛题目详情
    getContestProblem(contestName, problemName) {
      return api.get('/contest/problem', { params: { contest_name: contestName, problem_name: problemName } });
    },
    // 向比赛添加题目
    addProblem(contestName, problemName, score) {
      return api.post('/contest/problem', {
        contestName,
        problemName,
        score
      });
    },
    // 从比赛删除题目
    deleteProblem(contestName, problemName) {
      return api.delete('/contest/problem', {
        data: {
          contestName,
          problemName
        }
      });
    },
    // 参加比赛
    attendContest(contestName, password) {
      return api.post(`/contest/attend?contest_name=${contestName}`, { password });
    },
    // 获取已参加比赛
    getAttendedContests(userName) {
      return api.get('/contest/attended', { params: { user_name: userName } });
    },
    // 提交比赛代码
    submitContestCode(contestName, problemName, lang, code) {
      return api.post(`/contest/submit?language=${lang}&problem_name=${problemName}&contest_name=${contestName}`, code, {
        headers: {
          'Content-Type': 'text/plain'
        }
      });
    },
    // 获取比赛排行榜
    getContestBoard(contestName) {
      return api.get('/contest/board', { params: { contest_name: contestName } });
    },
    // 获取比赛提交记录
    getContestSubmissions(contestName, page = 1, size = 10) {
      const start = (page - 1) * size;
      return api.get('/contest/submits', { params: { contest_name: contestName, start, limit: size } });
    }
  },

  // 建议模块
  advice: {
    // 获取代码建议
    getAdvice(userFile) {
      return api.post('/advice', userFile);
    }
  },

  // 系统模块
  sys: {
    // 获取文档
    getDoc() {
      return api.get('/sys/doc');
    },
    // 更新文档
    updateDoc(doc) {
      return api.put('/sys/doc', doc);
    },
    // 获取日志
    getLogs() {
      return api.get('/sys/log/list');
    },
    // 获取数据库备份
    getMysqlDump() {
      return api.get('/sys/mysqldump');
    }
  }
};

export default apiService;
