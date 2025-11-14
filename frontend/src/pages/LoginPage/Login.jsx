import "./Login.css"

import {useState} from 'react';
import { LockOutlined, MailOutlined, ForkOutlined, HeartFilled, PlayCircleOutlined, UserOutlined, UserAddOutlined } from '@ant-design/icons';
import { Card, Form, Input, Button, Typography, Checkbox, Space } from 'antd';
const { Title, Text, Link } = Typography;

const Login = () => {

    const [isRegister, setIsRegister] = useState(false); 

    const toggleForm = () => {
        setIsRegister((prev) => !prev);
    };

    const onLogin = async() =>{
        console.log("Logged in")
    }

    return(
        <div className="login-mainLayout">

            {/* ---------------------------------------------------- */}
            {/* LEFT PANEL                                           */}
            {/* ---------------------------------------------------- */}

            <div className="login-heroPanel">
                <div className="login-heroPattern" />
                <FlavorSwirlSVG />
                <div className="login-heroContent">
                    <HeartFilled style={{ fontSize: 96, color: 'white', marginBottom: 10, textShadow: '0 4px 8px rgba(0,0,0,0.1)' }} />

                    <Title level={1} style={{ color: 'white', fontWeight: 800, marginBottom: 12, fontSize: '3.5rem' }}>
                        The World's Kitchen, Delivered.
                    </Title>

                    <div className="login-testimonialBlock">
                        <Text style={{ color: 'white', fontSize: 18, lineHeight: '28px', display: 'block' }}>
                            "RecipeConnect transformed my cooking journey. I found my signature dish here and learned professional plating techniques!" 
                        </Text>
                        <Text style={{ color: 'white', fontSize: 14, fontWeight: 'bold', display: 'block', marginTop: 10 }}>
                            — Alex, Happy Home Chef
                        </Text>
                    </div>

                    <Space size="large" style={{ marginTop: 20 }}>
                        <Button 
                            type="primary"
                            size="large"
                            icon={<PlayCircleOutlined />}
                            className="login-secondaryButton"
                            onClick={() => console.log('Viewing Demo...')}
                        >
                            Watch Demo
                        </Button>
                        <Button 
                            type="default"
                            size="large"
                            className="login-secondaryButton-explore"
                            onClick={() => console.log('Explore Trending Recipes...')}
                        >
                            Explore Trending
                        </Button>
                    </Space>
                </div>
            </div>

            {/* ---------------------------------------------------- */}
            {/* RIGHT PANEL  -- Login                                */}
            {/* ---------------------------------------------------- */}

            {
                !isRegister && (
                    <div className="login-formPanel">
                        <Card className="login-card">
                            {/* --- Custom Header/Logo --- */}
                            <div style={{ textAlign: 'center', marginBottom: '32px' }}>
                                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', marginBottom: '8px' }}>
                                    <ForkOutlined className="login-logoIcon"/>
                                    <Title level={2} style={{ margin: 0, color: '#333' }}>
                                        RecipeConnect
                                    </Title>
                                </div>
                                <Text type="secondary" style={{ fontSize: '14px', color: '#666' }}>
                                    Welcome back to your kitchen community.
                                </Text>
                            </div>
            
                            {/* --- Login Form --- */}
                            <Form
                                name="recipe_login"
                                initialValues={{ remember: true }}
                                onFinish={onLogin}
                                layout="vertical"
                                requiredMark={false}
                            >
                                {/* Email Field */}
                                <Form.Item
                                    label={<Text style={{ fontWeight: '600' }}>Email Address</Text>}
                                    name="email"
                                    rules={[
                                        { required: true, message: 'Please input your Email!' },
                                        { type: 'email', message: 'The input is not a valid E-mail!' }
                                    ]}
                                >
                                <Input
                                    prefix={<MailOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                    placeholder="user@example.com"
                                    size="large"
                                    style={{ borderRadius: '8px' }}
                                />
                                </Form.Item>

                                {/* Password Field */}
                                <Form.Item
                                    label={<Text style={{ fontWeight: '600' }}>Password</Text>}
                                    name="password"
                                    rules={[{ required: true, message: 'Please input your Password!' }]}
                                >
                                    <Input.Password
                                        prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                        placeholder="Password"
                                        size="large"
                                        style={{ borderRadius: '8px' }}
                                    />
                                </Form.Item>

                                {/* Remember Me and Forgot Password Section */}
                                <Form.Item>
                                <Form.Item name="remember" valuePropName="checked" noStyle>
                                    <Checkbox style={{ color: '#666' }}>Keep me logged in</Checkbox>
                                </Form.Item>
                                <Link 
                                    href="/forgot-password" 
                                    className="login-link-forgotPassword"
                                >
                                    Forgot password?
                                </Link>
                                </Form.Item>

                                {/* Login Button */}
                                <Form.Item style={{ marginTop: '30px' }}>
                                    <Button 
                                        type="default" 
                                        htmlType="submit" 
                                        className="login-buttonLogin"
                                        block
                                    >
                                        Log In
                                    </Button>
                                </Form.Item>

                                {/* Link to Registration */}
                                <div style={{ textAlign: 'center', marginTop: '24px' }}>
                                <Text style={{ color: '#666' }}>
                                    New to RecipeConnect?{' '}
                                    <a onClick={toggleForm} className="login-link-createAccount" style={{ cursor: 'pointer' }}>
                                        Create an account
                                    </a>
                                </Text>
                                </div>
                            </Form>
                        </Card>
                    </div>
                )
            }
            
            {/* ---------------------------------------------------- */}
            {/* RIGHT PANEL - Register                               */}
            {/* ---------------------------------------------------- */}

            {
                isRegister && (
                    <div className="login-formPanel">
                        <Card className="login-card">
                            {/* --- Custom Header/Logo --- */}
                            <div style={{ textAlign: 'center', marginBottom: '32px' }}>
                                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', marginBottom: '8px' }}>
                                    <ForkOutlined className="login-logoIcon"/>
                                    <Title level={2} style={{ margin: 0, color: '#333' }}>
                                        RecipeConnect
                                    </Title>
                                </div>
                                <Text type="secondary" style={{ fontSize: '14px', color: '#666' }}>
                                    Join our kitchen community!
                                </Text>
                            </div>
            
                            {/* --- Login Form --- */}
                            <Form
                                name="recipe_register"
                                initialValues={{ remember: true }}
                                // onFinish={onFinish}
                                // onFinishFailed={onFinishFailed}
                                layout="vertical"
                                requiredMark={false}
                            >
                                {/* Name Field */}
                                <Form.Item
                                    label={<Text style={{ fontWeight: '600' }}>Full name</Text>}
                                    name="fullname"
                                    rules={[
                                        { required: true, message: 'Please input your Full Name!' }
                                    ]}
                                >
                                <Input
                                    prefix={<UserOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                    placeholder="Pratik Kumar"
                                    size="large"
                                    style={{ borderRadius: '8px' }}
                                />
                                </Form.Item>

                                {/* Username Field */}
                                <Form.Item
                                    label={<Text style={{ fontWeight: '600' }}>Username</Text>}
                                    name="username"
                                    rules={[
                                        { required: true, message: 'Please input your Username!' }
                                    ]}
                                >
                                <Input
                                    prefix={<UserAddOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                    placeholder="Pratik_kumar17"
                                    size="large"
                                    style={{ borderRadius: '8px' }}
                                />
                                </Form.Item>

                                {/* Email Field */}
                                <Form.Item
                                    label={<Text style={{ fontWeight: '600' }}>Email Address</Text>}
                                    name="email"
                                    rules={[
                                        { required: true, message: 'Please input your Email!' },
                                        { type: 'email', message: 'The input is not a valid E-mail!' }
                                    ]}
                                >
                                <Input
                                    prefix={<MailOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                    placeholder="user@example.com"
                                    size="large"
                                    style={{ borderRadius: '8px' }}
                                />
                                </Form.Item>

                                {/* Password Field */}
                                <Form.Item
                                    label={<Text style={{ fontWeight: '600' }}>Password</Text>}
                                    name="password"
                                    rules={[{ required: true, message: 'Please input your Password!' }]}
                                >
                                    <Input.Password
                                        prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                        placeholder="Password"
                                        size="large"
                                        style={{ borderRadius: '8px' }}
                                    />
                                </Form.Item>

                                {/* Terms of Service Checkbox */}
                                <Form.Item
                                    name="agreement"
                                    valuePropName="checked"
                                    rules={[
                                        {
                                        validator: (_, value) =>
                                            value ? Promise.resolve() : Promise.reject(new Error('You must accept the terms and conditions.')),
                                        },
                                    ]}
                                >
                                    <Checkbox className="!text-gray-600 !text-xs">
                                        I agree to the RecipeConnect{' '}
                                        <Link href="/terms" className="!text-[#FF6F61] !font-medium" target="_blank">
                                            Terms of Service
                                        </Link>
                                    </Checkbox>
                                </Form.Item>

                                {/* Register Button */}
                                <Form.Item style={{ marginTop: '30px' }}>
                                    <Button 
                                        type="default" 
                                        htmlType="submit" 
                                        className="login-buttonLogin"
                                        block
                                    >
                                        Register
                                    </Button>
                                </Form.Item>

                                {/* Link to Registration */}
                                <div style={{ textAlign: 'center', marginTop: '24px' }}>
                                <Text style={{ color: '#666' }}>
                                    Already have an account? {' '}
                                    <a onClick={toggleForm} className="login-link-createAccount" style={{ cursor: 'pointer' }}>
                                        Login
                                    </a>
                                </Text>
                                </div>
                            </Form>
                        </Card>
                    </div>
                )
            }
            
        </div>
    )
}

const FlavorSwirlSVG = () => (
  <svg 
    style={{ position: 'absolute', top: '0', left: '0', width: '100%', height: '100%', zIndex: 5, opacity: 0.15 }} 
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