<template>
  <div class="login-bg">
    <form class="container" @submit.prevent="handleLogin">
      <h1 class="login-title">FinSmart</h1>

      <section class="input-box">
        <input 
          type="text" 
          name="username" 
          placeholder="Username"
          v-model="loginForm.username"
          required
        >
        <i class="bx bxs-user"></i>
      </section>

      <section class="input-box">
        <input 
          type="password" 
          name="password" 
          placeholder="Password"
          v-model="loginForm.password"
          required
        >
        <i class='bx bxs-lock-alt'></i>
      </section>

      <section class="remember-forgot-box">
        <div class="remember-me">
          <input type="checkbox" name="remember-me" id="remember-me">
          <label for="remember-me">
            <h5>Remember me</h5>
          </label>
        </div>
        <a class="forgot-password" href="#">
          <h5>Forgot password?</h5>
        </a>
      </section>

      <button class="login-button" type="submit" :disabled="loading">
        <span v-if="!loading">Login</span>
        <span v-else>Logging in...</span>
      </button>

      <h5 class="dont-have-an-account">
        Don't have an account?
        <a href="#" @click.prevent="showRegister = true"><b>Register</b></a>
      </h5>
    </form>

    <!-- 注册弹窗 -->
    <el-dialog
      v-model="showRegister"
      title="用户注册"
      width="400px"
      :before-close="handleCloseRegister"
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRegister = false">取消</el-button>
          <el-button type="primary" :loading="registerLoading" @click="handleRegister">
            注册
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/auth'

const router = useRouter()

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 注册表单
const registerFormRef = ref()
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loading = ref(false)
const registerLoading = ref(false)
const showRegister = ref(false)

// 登录处理
const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  try {
    loading.value = true
    
    const result = await login(loginForm)
    
    // 保存token
    localStorage.setItem('token', result.token)
    
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    
    await register({
      username: registerForm.username,
      password: registerForm.password
    })
    
    ElMessage.success('注册成功，请登录')
    showRegister.value = false
    
    // 清空注册表单
    Object.assign(registerForm, {
      username: '',
      password: '',
      confirmPassword: ''
    })
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    registerLoading.value = false
  }
}

// 关闭注册弹窗
const handleCloseRegister = () => {
  showRegister.value = false
  Object.assign(registerForm, {
    username: '',
    password: '',
    confirmPassword: ''
  })
}
</script>

<style lang="scss" scoped>
* {
  font-family: "Poppins", sans-serif;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.login-bg {
  background: linear-gradient(135deg, #E6C4FE 0%, #A8E6FF 50%, #C4D3FE 100%);
  width: 100%;
  height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="rgba(255,255,255,0.03)"/><circle cx="75" cy="75" r="1" fill="rgba(255,255,255,0.03)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
    pointer-events: none;
  }
}

.container {
  background: rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(25px);
  width: 320px;
  padding: 32px;
  border-radius: 16px;
  border: solid 2px rgba(255, 255, 255, 0.2);
  box-shadow: 0px 0px 40px 20px rgba(0, 0, 0, 0.15);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.login-title {
  margin-bottom: 32px;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #D2C4FE 0%, #A8E6FF 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.input-box {
  display: flex;
  width: 100%;
  position: relative;
  margin-top: 20px;
}

.input-box input {
  width: 100%;
  padding: 14px 16px 14px 45px;
  border-radius: 12px;
  border: solid 2px rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.08);
  outline: none;
  caret-color: #A8E6FF;
  color: white;
  font-weight: 500;
  font-size: 15px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.input-box input:focus {
  border: solid 2px #A8E6FF;
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 0 20px rgba(168, 230, 255, 0.3);
}

.input-box input::placeholder {
  color: rgba(255, 255, 255, 0.6);
  font-weight: 400;
}

.input-box input::-ms-reveal {
  filter: invert(100%);
}

.input-box i {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  left: 16px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 18px;
  transition: all 0.3s ease;
}

.input-box input:focus + i {
  color: #A8E6FF;
  transform: translateY(-50%) scale(1.1);
}

.remember-forgot-box {
  display: flex;
  width: 100%;
  margin-top: 20px;
  justify-content: space-between;
  align-items: center;
}

.remember-forgot-box h5 {
  font-weight: normal;
  font-size: 13px;
}

.remember-me {
  display: flex;
  gap: 8px;
  align-items: center;
}

.remember-me input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #A8E6FF;
}

.forgot-password {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: 13px;
  transition: all 0.3s ease;
}

.forgot-password:hover {
  color: #A8E6FF;
  text-decoration: underline;
}

.login-button {
  width: 100%;
  margin-top: 28px;
  padding: 14px 0;
  background: linear-gradient(135deg, #D2C4FE 0%, #A8E6FF 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  outline: transparent 3px solid;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.login-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.login-button:hover::before {
  left: 100%;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(210, 196, 254, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.login-button:focus {
  outline: #A8E6FF 3px solid;
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.dont-have-an-account {
  font-weight: normal;
  margin-top: 24px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
}

.dont-have-an-account a {
  text-decoration: none;
  color: #A8E6FF;
  font-weight: 600;
  transition: all 0.3s ease;
}

.dont-have-an-account a:hover {
  color: #D2C4FE;
  text-decoration: underline;
}

// 响应式设计
@media (max-width: 480px) {
  .container {
    width: 90%;
    padding: 24px;
    margin: 20px;
  }
  
  .login-title {
    font-size: 28px;
    margin-bottom: 24px;
  }
  
  .input-box input {
    padding: 12px 16px 12px 45px;
    font-size: 14px;
  }
  
  .login-button {
    padding: 12px 0;
    font-size: 15px;
  }
}

// 动画效果
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.container {
  animation: fadeInUp 0.6s ease-out;
}

// Element Plus 弹窗样式覆盖
:deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(210, 196, 254, 0.3);
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #E6C4FE 0%, #A8E6FF 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: #5f41d4;
  font-weight: 600;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #D2C4FE 0%, #A8E6FF 100%);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #C4D3FE 0%, #98D6FE 100%);
}
</style>
