import "./Login.css";

import { useAuth } from "../../auth/AuthContext";
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import {
  LockOutlined,
  MailOutlined,
  ForkOutlined,
  HeartFilled,
  PlayCircleOutlined,
  UserOutlined,
  UserAddOutlined,
} from "@ant-design/icons";
import {
  Card,
  Form,
  Input,
  Button,
  Typography,
  Checkbox,
  Space,
  notification,
} from "antd";
import NonAuthNavbar from "../../components/Navbar/NonAuthNavbar/NonAuthNavbar";
import { ChefHat } from "lucide-react";
const { Title, Text, Link } = Typography;

const Login = () => {
  const [api, contextHolder] = notification.useNotification();
  const openNotification = (pauseOnHover, type, message, description) => () => {
    api.open({
      message,
      description,
      showProgress: true,
      pauseOnHover,
      type,
    });
  };
  const [form] = Form.useForm();
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [registerForm, setRegisterForm] = useState({
    fullName: "",
    username: "",
    email: "",
    password: "",
  });
  const { user, setUser } = useAuth();
  const navigate = useNavigate();
  const [loginButtonLoading, setLoginButtonLoading] = useState(false);
  const [registerButtonLoading, setRegisterButtonLoading] = useState(false);

  const toggleForm = () => {
    setIsRegister((prev) => !prev);
  };

  useEffect(() => {
    if (user.isAuthenticated) {
      switch (user.role) {
        case "USER":
          navigate("/homepage");
          break;
        default:
          navigate("/unauthorized");
      }
    }
  }, [user, navigate]);

  const loginUser = async (email, password) => {
    setLoginButtonLoading(true);
    try {
      const res = await fetch("http://localhost:8089/api/v1/users/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (res.status !== 200) {
        throw new Error("Login failed");
      }
      if (res.status === 200) {
        form.resetFields();
      }
      return res.json();
    } catch (err) {
      throw err;
    } finally {
      setLoginButtonLoading(false);
    }
  };

  const onLogin = async () => {
    try {
      const { token, role } = await loginUser(email, password);
      setUser({ isAuthenticated: true, role, token });
      localStorage.setItem("token", token);
      localStorage.setItem("role", role);
      switch (role) {
        case "USER":
          navigate("/homepage");
          break;
        default:
          navigate("/unauthorized");
      }
    } catch (err) {
      openNotification(
        false,
        "error",
        "Login failed",
        "Please check your email and password and try again."
      )();
    }
  };

  const onRegister = async () => {
    try {
      setRegisterButtonLoading(true);
      const response = await fetch(
        "http://localhost:8089/api/v1/users/register",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(registerForm),
        }
      );

      const data = await response.json();

      if (response.status === 500 || response.status === 400) {
        throw new Error(data.message || data.error || "Registration failed");
      }

      openNotification(
        true,
        "success",
        "Registration successful",
        "Your account has been created. Please log in."
      )();
      setIsRegister(false);
      setRegisterForm({
        fullName: "",
        username: "",
        email: "",
        password: "",
      });
      form.resetFields();
    } catch (err) {
      openNotification(true, "error", "Registration failed", err.message)();
    } finally {
      setRegisterButtonLoading(false);
    }
  };

  return (
    <>
      <NonAuthNavbar activeButton="login" />
      {contextHolder}
      <div className="login-mainLayout">
        {contextHolder}

        {/* ---------------------------------------------------- */}
        {/* LEFT PANEL                                           */}
        {/* ---------------------------------------------------- */}

        <div className="login-heroPanel">
          <div className="login-heroPattern" />
          <FlavorSwirlSVG />
          <div className="login-heroContent">
            <HeartFilled
              style={{
                fontSize: 96,
                color: "white",
                marginBottom: 10,
                textShadow: "0 4px 8px rgba(0,0,0,0.1)",
              }}
            />

            <Title
              level={1}
              style={{
                color: "white",
                fontWeight: 800,
                marginBottom: 12,
                fontSize: "3.5rem",
              }}
            >
              The World's Kitchen, Delivered.
            </Title>

            <div className="login-testimonialBlock">
              <Text
                style={{
                  color: "white",
                  fontSize: 18,
                  lineHeight: "28px",
                  display: "block",
                }}
              >
                "Cooknect transformed my cooking journey. I found my signature
                dish here and learned professional plating techniques!"
              </Text>
              <Text
                style={{
                  color: "white",
                  fontSize: 14,
                  fontWeight: "bold",
                  display: "block",
                  marginTop: 10,
                }}
              >
                — Alex, Happy Home Chef
              </Text>
            </div>

            <Space size="large" style={{ marginTop: 20 }}>
              <Button
                type="primary"
                size="large"
                icon={<PlayCircleOutlined />}
                className="login-secondaryButton"
                onClick={() => console.log("Viewing Demo...")}
              >
                Watch Demo
              </Button>
              <Button
                type="default"
                size="large"
                className="login-secondaryButton-explore"
                onClick={() => console.log("Explore Trending Recipes...")}
              >
                Explore Trending
              </Button>
            </Space>
          </div>
        </div>

        {/* ---------------------------------------------------- */}
        {/* RIGHT PANEL  -- Login                                */}
        {/* ---------------------------------------------------- */}

        {!isRegister && (
          <div className="login-formPanel">
            <Card className="login-card">
              <div style={{ textAlign: "center", marginBottom: "32px" }}>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    marginBottom: "8px",
                  }}
                >
                  <ChefHat
                    className="login-logoIcon"
                    style={{
                      fontSize: 48,
                      width: 48,
                      height: 48,
                    }}
                  />
                  <Title level={2} style={{ margin: 0, color: "#333" }}>
                    Cooknect
                  </Title>
                </div>
                <Text
                  type="secondary"
                  style={{ fontSize: "14px", color: "#666" }}
                >
                  Welcome back to your kitchen community.
                </Text>
              </div>
              <Form
                form={form}
                name="recipe_login"
                initialValues={{ remember: true }}
                onFinish={onLogin}
                layout="vertical"
                requiredMark={false}
              >
                {/* Email Field */}
                <Form.Item
                  label={
                    <Text style={{ fontWeight: "600" }}>Email Address</Text>
                  }
                  name="email"
                  rules={[
                    { required: true, message: "Please input your Email!" },
                    {
                      type: "email",
                      message: "The input is not a valid E-mail!",
                    },
                  ]}
                >
                  <Input
                    prefix={
                      <MailOutlined style={{ color: "rgba(0,0,0,.25)" }} />
                    }
                    placeholder="user@example.com"
                    size="large"
                    style={{ borderRadius: "8px" }}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                  />
                </Form.Item>

                {/* Password Field */}
                <Form.Item
                  label={<Text style={{ fontWeight: "600" }}>Password</Text>}
                  name="password"
                  rules={[
                    { required: true, message: "Please input your Password!" },
                  ]}
                >
                  <Input.Password
                    prefix={
                      <LockOutlined style={{ color: "rgba(0,0,0,.25)" }} />
                    }
                    placeholder="Password"
                    size="large"
                    style={{ borderRadius: "8px" }}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </Form.Item>

                {/* Remember Me and Forgot Password Section */}
                <Form.Item>
                  <Form.Item name="remember" valuePropName="checked" noStyle>
                    <Checkbox style={{ color: "#666" }}>
                      Keep me logged in
                    </Checkbox>
                  </Form.Item>
                  <Link
                    href="/forgot-password"
                    className="login-link-forgotPassword"
                  >
                    Forgot password?
                  </Link>
                </Form.Item>

                {/* Login Button */}
                <Form.Item style={{ marginTop: "30px" }}>
                  <Button
                    type="default"
                    htmlType="submit"
                    className="login-buttonLogin"
                    block
                    loading={loginButtonLoading}
                  >
                    Log In
                  </Button>
                </Form.Item>

                {/* Link to Registration */}
                <div style={{ textAlign: "center", marginTop: "24px" }}>
                  <Text style={{ color: "#666" }}>
                    New to RecipeConnect?{" "}
                    <a
                      onClick={toggleForm}
                      className="login-link-createAccount"
                      style={{ cursor: "pointer" }}
                    >
                      Create an account
                    </a>
                  </Text>
                </div>
              </Form>
            </Card>
          </div>
        )}

        {/* ---------------------------------------------------- */}
        {/* RIGHT PANEL - Register                               */}
        {/* ---------------------------------------------------- */}

        {isRegister && (
          <div className="login-formPanel">
            <Card className="login-card">
              {/* --- Custom Header/Logo --- */}
              <div style={{ textAlign: "center", marginBottom: "32px" }}>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    marginBottom: "8px",
                  }}
                >
                  <ForkOutlined className="login-logoIcon" />
                  <Title level={2} style={{ margin: 0, color: "#333" }}>
                    RecipeConnect
                  </Title>
                </div>
                <Text
                  type="secondary"
                  style={{ fontSize: "14px", color: "#666" }}
                >
                  Join our kitchen community!
                </Text>
              </div>

              {/* --- Register Form --- */}
              <Form
                form={form}
                name="recipe_register"
                initialValues={{ remember: true }}
                onFinish={onRegister}
                layout="vertical"
                requiredMark={false}
              >
                {/* Name Field */}
                <Form.Item
                  label={<Text style={{ fontWeight: "600" }}>Full name</Text>}
                  name="fullName"
                  rules={[
                    { required: true, message: "Please input your Full Name!" },
                  ]}
                >
                  <Input
                    prefix={
                      <UserOutlined style={{ color: "rgba(0,0,0,.25)" }} />
                    }
                    placeholder="Pratik Kumar"
                    size="large"
                    style={{ borderRadius: "8px" }}
                    value={registerForm.fullName}
                    onChange={(e) =>
                      setRegisterForm({
                        ...registerForm,
                        fullName: e.target.value,
                      })
                    }
                  />
                </Form.Item>

                {/* Username Field */}
                <Form.Item
                  label={<Text style={{ fontWeight: "600" }}>Username</Text>}
                  name="username"
                  rules={[
                    { required: true, message: "Please input your Username!" },
                  ]}
                >
                  <Input
                    prefix={
                      <UserAddOutlined style={{ color: "rgba(0,0,0,.25)" }} />
                    }
                    placeholder="Pratik_kumar17"
                    size="large"
                    style={{ borderRadius: "8px" }}
                    value={registerForm.username}
                    onChange={(e) =>
                      setRegisterForm({
                        ...registerForm,
                        username: e.target.value,
                      })
                    }
                  />
                </Form.Item>

                {/* Email Field */}
                <Form.Item
                  label={
                    <Text style={{ fontWeight: "600" }}>Email Address</Text>
                  }
                  name="email"
                  rules={[
                    { required: true, message: "Please input your Email!" },
                    {
                      type: "email",
                      message: "The input is not a valid E-mail!",
                    },
                  ]}
                >
                  <Input
                    prefix={
                      <MailOutlined style={{ color: "rgba(0,0,0,.25)" }} />
                    }
                    placeholder="user@example.com"
                    size="large"
                    style={{ borderRadius: "8px" }}
                    value={registerForm.email}
                    onChange={(e) =>
                      setRegisterForm({
                        ...registerForm,
                        email: e.target.value,
                      })
                    }
                  />
                </Form.Item>

                {/* Password Field */}
                <Form.Item
                  label={<Text style={{ fontWeight: "600" }}>Password</Text>}
                  name="password"
                  rules={[
                    { required: true, message: "Please input your Password!" },
                  ]}
                >
                  <Input.Password
                    prefix={
                      <LockOutlined style={{ color: "rgba(0,0,0,.25)" }} />
                    }
                    placeholder="Password"
                    size="large"
                    style={{ borderRadius: "8px" }}
                    value={registerForm.password}
                    onChange={(e) =>
                      setRegisterForm({
                        ...registerForm,
                        password: e.target.value,
                      })
                    }
                  />
                </Form.Item>

                {/* Terms of Service Checkbox */}
                <Form.Item
                  name="agreement"
                  valuePropName="checked"
                  rules={[
                    {
                      validator: (_, value) =>
                        value
                          ? Promise.resolve()
                          : Promise.reject(
                              new Error(
                                "You must accept the terms and conditions."
                              )
                            ),
                    },
                  ]}
                >
                  <Checkbox className="!text-gray-600 !text-xs">
                    I agree to the RecipeConnect{" "}
                    <Link
                      href="/terms"
                      className="!text-[#FF6F61] !font-medium"
                      target="_blank"
                    >
                      Terms of Service
                    </Link>
                  </Checkbox>
                </Form.Item>

                {/* Register Button */}
                <Form.Item style={{ marginTop: "30px" }}>
                  <Button
                    type="default"
                    htmlType="submit"
                    className="login-buttonLogin"
                    block
                    loading={registerButtonLoading}
                  >
                    Register
                  </Button>
                </Form.Item>

                {/* Link to Registration */}
                <div style={{ textAlign: "center", marginTop: "24px" }}>
                  <Text style={{ color: "#666" }}>
                    Already have an account?{" "}
                    <a
                      onClick={toggleForm}
                      className="login-link-createAccount"
                      style={{ cursor: "pointer" }}
                    >
                      Login
                    </a>
                  </Text>
                </div>
              </Form>
            </Card>
          </div>
        )}
      </div>
    </>
  );
};

const FlavorSwirlSVG = () => (
  <svg
    style={{
      position: "absolute",
      top: "0",
      left: "0",
      width: "100%",
      height: "100%",
      zIndex: 5,
      opacity: 0.15,
    }}
    viewBox="0 0 1000 1000"
    xmlns="http://www.w3.org/2000/svg"
    preserveAspectRatio="xMidYMid slice"
  >
    <path
      d="M300 100 Q 500 50 700 100 T 900 200 L 950 300 Q 850 450 750 500 T 500 650 Q 300 750 100 700 L 50 600 Q 150 400 250 350 T 400 200 Z"
      fill="white"
    />
    <path
      d="M150 850 Q 350 900 600 800 T 850 650"
      fill="none"
      stroke="white"
      strokeWidth="50"
      strokeOpacity="0.5"
    />
  </svg>
);

export default Login;
