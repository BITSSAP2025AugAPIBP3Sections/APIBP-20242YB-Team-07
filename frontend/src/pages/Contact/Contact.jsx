import React from 'react';
import { Layout, Typography, Button, Card, Row, Col, theme, Form, Input, Space, Divider, notification } from 'antd';
import { Mail, Phone, MapPin, Send, MessageSquare } from 'lucide-react';
import NonAuthNavbar from '../../components/Navbar/NonAuthNavbar/NonAuthNavbar';

const { Content, Footer } = Layout;
const { Title, Paragraph } = Typography;
const { TextArea } = Input;

// --- Mock Contact Info ---
const contactInfo = [
  { icon: <Mail size={24} color="#1890ff" />, title: 'Email Support', detail: '2024sl93061@wilp.bits-pilani.ac.in', link: 'mailto:2024sl93061@wilp.bits-pilani.ac.in' },
  { icon: <Phone size={24} color="#52c41a" />, title: 'Phone/Sales', detail: '+91-6203243724', link: 'tel:91-6203243724' },
  { icon: <MapPin size={24} color="#faad14" />, title: 'Our Location', detail: 'SAP Labs, Whitefield, Bangalore', link: 'https://share.google/RkX4nFmy5zSpM5p6j' },
];

// --- CSS Styles using JS Objects ---
const styles = {
  layout: {
    minHeight: '100vh',
    fontFamily: 'Inter, sans-serif',
  },
  header: {
    padding: '0 24px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    zIndex: 10,
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.05)',
    height: '64px',
  },
  logo: {
    display: 'flex',
    alignItems: 'center',
    fontSize: '24px',
    fontWeight: '800',
    color: '#1890ff',
  },
  heroSection: (token) => ({
    padding: '60px 24px',
    textAlign: 'center',
    background: token.colorBgLayout,
    borderBottom: `1px solid ${token.colorBorderSecondary}`,
  }),
  contactSection: {
    padding: '80px 24px',
    maxWidth: '1200px',
    margin: '0 auto',
  },
  infoCard: {
    height: '100%',
    textAlign: 'left',
    padding: '20px',
    borderRadius: '12px',
    borderLeft: '5px solid #e8e8e8',
    transition: 'border-left-color 0.3s ease-in-out',
    cursor: 'pointer',
  },
  footer: {
    textAlign: 'center',
    padding: '24px 0',
    backgroundColor: '#f0f2f5',
    color: '#888',
    borderTop: '1px solid #e8e8e8',
  }
};

// --- Helper Components ---
const ContactInfoCard = ({ icon, title, detail, link, primaryColor }) => {
    const [isHovered, setIsHovered] = React.useState(false);

    const hoverStyle = {
        borderLeftColor: isHovered ? primaryColor : '#e8e8e8',
        boxShadow: isHovered ? '0 10px 20px rgba(0, 0, 0, 0.1)' : '0 4px 8px rgba(0, 0, 0, 0.05)',
        transform: isHovered ? 'translateY(-3px)' : 'translateY(0)',
    };

    return (
        <a href={link} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'none', display: 'block', height: '100%' }}>
            <Card
                hoverable
                style={{ ...styles.infoCard, ...hoverStyle }}
                onMouseEnter={() => setIsHovered(true)}
                onMouseLeave={() => setIsHovered(false)}
                bodyStyle={{ display: 'flex', alignItems: 'flex-start' }}
            >
                <div style={{ minWidth: '40px', paddingTop: '4px' }}>
                    {icon}
                </div>
                <div style={{ marginLeft: '16px' }}>
                    <Title level={4} style={{ margin: 0, fontSize: '18px' }}>{title}</Title>
                    <Paragraph style={{ margin: 0, color: '#555' }}>{detail}</Paragraph>
                </div>
            </Card>
        </a>
    );
};


// --- Main Application Component ---
const Contact = () => {
  const [api, contextHolder] = notification.useNotification();
  const { token } = theme.useToken();
  const [queryData, setQueryData] = React.useState({
    name: '',
    email: '',
    subject: '',
    message: ''
  });
  const [form] = Form.useForm();

  const openNotification = (pauseOnHover, type, message, description) => () => {
    api.open({
      message,
      description,
      showProgress: true,
      pauseOnHover,
      type,
    });
  };

  const onFinish = () => {
    try{
      const response = fetch('http://localhost:8089/api/v1/users/query',{
        method:'POST',
        headers:{
          'Content-Type':'application/json'
        },
        body:JSON.stringify(queryData)
      });
      openNotification(true, 'success', 'Message Sent', 'Your message has been sent successfully. We will get back to you soon!')();
      setQueryData({
        name: '',
        email: '',
        subject: '',
        message: ''
      });
      form.resetFields();
    }
    catch(err){
      openNotification(true, 'error', 'Submission Failed', 'There was an error sending your message. Please try again later.')();
    }
  };
  
  // NOTE: Using a simple `alert()` placeholder for this demo, as per guidelines, 
  // a custom modal UI would be used in a production environment.

  return (
    <Layout style={styles.layout}>
      {contextHolder}
      {/* 1. Header (Static Nav) */}
      <NonAuthNavbar activeButton='contact' />

      <Content>
        {/* 2. Hero Section */}
        <div style={styles.heroSection(token)}>
          <Title level={1} style={{ fontSize: 'clamp(32px, 7vw, 48px)', fontWeight: '900', marginBottom: '8px' }}>
            We're Ready to Help
          </Title>
          <Paragraph style={{ fontSize: '18px', maxWidth: '700px', margin: '0 auto' }}>
            Whether you have a question, need support, or want to give feedback, our team is here to listen.
          </Paragraph>
        </div>
        
        {/* 3. Contact Form and Info Section */}
        <div style={styles.contactSection}>
            <Row gutter={[48, 48]}>
                
                {/* Left Column: Contact Form */}
                <Col xs={24} lg={14}>
                    <Card style={{ borderRadius: '16px', boxShadow: '0 10px 30px rgba(0, 0, 0, 0.1)' }}>
                        <Title level={3} style={{ marginTop: 0, display: 'flex', alignItems: 'center' }}>
                            <MessageSquare size={24} style={{ marginRight: '10px', color: token.colorPrimary }} />
                            Send Us a Message
                        </Title>
                        <Paragraph style={{ color: token.colorTextSecondary, marginBottom: '24px' }}>
                            Fill out the form below and we'll get back to you within 24 hours.
                        </Paragraph>

                        <Form
                            form={form}
                            name="contact_form"
                            onFinish={onFinish}
                            layout="vertical"
                            requiredMark={false}
                        >
                            <Form.Item
                                name="name"
                                label="Your Name"
                                rules={[{ required: true, message: 'Please enter your name!' }]}
                                value={queryData.name}
                                onChange ={(e)=>setQueryData({...queryData,name:e.target.value})}
                            >
                                <Input size="large" placeholder="John Doe" />
                            </Form.Item>

                            <Form.Item
                                name="email"
                                label="Email Address"
                                rules={[{ required: true, message: 'Please enter your email!', type: 'email' }]}
                                value={queryData.email}
                                onChange ={(e)=>setQueryData({...queryData,email:e.target.value})}
                            >
                                <Input size="large" placeholder="you@example.com" />
                            </Form.Item>
                            
                            <Form.Item
                                name="subject"
                                label="Subject"
                                rules={[{ required: true, message: 'Please tell us the subject!' }]}
                                value={queryData.subject}
                                onChange ={(e)=>setQueryData({...queryData,subject:e.target.value})}
                            >
                                <Input size="large" placeholder="Account support, feedback, etc." />
                            </Form.Item>

                            <Form.Item
                                name="message"
                                label="Your Message"
                                rules={[{ required: true, message: 'Please enter your message!' }]}
                                value={queryData.message}
                                onChange ={(e)=>setQueryData({...queryData,message:e.target.value})}
                            >
                                <TextArea rows={6} placeholder="How can we help you today?" />
                            </Form.Item>

                            <Form.Item style={{ marginTop: '30px' }}>
                                <Button 
                                    type="primary" 
                                    htmlType="submit" 
                                    size="large" 
                                    block
                                    icon={<Send size={20} />}
                                    style={{ height: '50px', fontSize: '18px', backgroundColor: token.colorPrimary, fontWeight: '600' }}
                                >
                                    Send Message
                                </Button>
                            </Form.Item>
                        </Form>
                    </Card>
                </Col>

                {/* Right Column: Contact Details */}
                <Col xs={24} lg={10}>
                    <Title level={3} style={{ color: token.colorTextHeading, marginTop: 0 }}>
                        Connect with the Cooknect Team
                    </Title>
                    <Paragraph style={{ color: token.colorTextSecondary, marginBottom: '40px' }}>
                        You can also reach us through these direct channels.
                    </Paragraph>
                    
                    <Space direction="vertical" size={24} style={{ width: '100%' }}>
                        {contactInfo.map((info, index) => (
                            <ContactInfoCard 
                                key={index}
                                icon={info.icon}
                                title={info.title}
                                detail={info.detail}
                                link={info.link}
                                primaryColor={token.colorPrimary}
                            />
                        ))}
                    </Space>
                    
                    <Divider style={{ margin: '40px 0' }} />
                    
                    <Title level={4}>Business Hours</Title>
                    <Paragraph>
                        Our support staff is available Monday to Friday, 9:00 AM - 5:00 PM (IST).
                    </Paragraph>
                </Col>
            </Row>
        </div>
      </Content>

      {/* 4. Footer */}
      <Footer style={styles.footer}>
        Cooknect ©{new Date().getFullYear()} Created with ❤️ by APIBP Team 07
      </Footer>
    </Layout>
  );
};

export default Contact;