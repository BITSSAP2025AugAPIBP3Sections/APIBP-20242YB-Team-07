import React from 'react';
import { Layout, Typography, Button, Card, Row, Col, theme, Divider } from 'antd';
import { Rocket, Heart, Zap, Users, BookOpen } from 'lucide-react';
import NonAuthNavbar from '../../components/Navbar/NonAuthNavbar/NonAuthNavbar';
import { useNavigate } from 'react-router-dom';
const { Content, Footer } = Layout;
const { Title, Paragraph } = Typography;

// --- Mock Values ---
const coreValues = [
  { 
    icon: <Heart size={32} color="#1890ff" />, 
    title: 'Passion for Flavor', 
    description: 'We believe cooking should be an exciting and joyful experience. Our platform is built on a love for all things culinary.' 
  },
  { 
    icon: <Zap size={32} color="#52c41a" />, 
    title: 'Simplicity & Clarity', 
    description: 'Recipes should be easy to follow, not frustrating. We focus on clear steps and beautiful, functional design.' 
  },
  { 
    icon: <Users size={32} color="#faad14" />, 
    title: 'Community First', 
    description: 'Cookbook thrives on shared knowledge. We foster a supportive environment for cooks of all skill levels to connect.' 
  },
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
  missionSection: (token) => ({
    padding: '80px 24px',
    textAlign: 'center',
    background: token.colorBgLayout,
    borderBottom: `1px solid ${token.colorBorderSecondary}`,
  }),
  contentSection: {
    padding: '80px 24px',
    maxWidth: '1200px',
    margin: '0 auto',
  },
  card: {
    height: '100%',
    borderRadius: '12px',
    textAlign: 'center',
    transition: 'transform 0.3s ease-in-out',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.05)',
  },
  footer: {
    textAlign: 'center',
    padding: '24px 0',
    backgroundColor: '#f0f2f5',
    color: '#888',
    borderTop: '1px solid #e8e8e8',
  }
};

// --- Main Application Component ---
const About = () => {
  const { token } = theme.useToken();
  const accentColor = token.colorError; 
  const navigate = useNavigate();

  return (
    <Layout style={styles.layout}>
      {/* 1. Header (Static Nav) */}
      <NonAuthNavbar activeButton='about' />

      <Content>
        
        {/* 2. Mission & Vision Section */}
        <div style={styles.missionSection(token)}>
          <Title 
            level={1} 
            style={{ fontSize: 'clamp(36px, 7vw, 56px)', fontWeight: '900', marginBottom: '16px' }}
          >
            The Future of Cooking is Collaborative
          </Title>
          <Paragraph 
            style={{ fontSize: '20px', maxWidth: '800px', margin: '0 auto 40px' }}
          >
            Our mission is to empower every home cook to confidently explore, share, and master the art of cuisine, turning every meal into an adventure.
          </Paragraph>
          <Button 
            size="large" 
            icon={<Rocket size={20} />}
            style={{ height: '50px', fontSize: '18px', backgroundColor: accentColor, fontWeight: '600', color: '#fff' }}
            onClick={() => navigate('/login')}
          >
            Join the Movement
          </Button>
        </div>
        
        {/* 3. Our Story Section */}
        <div style={styles.contentSection}>
            <Row gutter={[48, 48]} align="middle">
                
                {/* Story Text */}
                <Col xs={24} lg={14}>
                    <Title level={2} style={{ color: token.colorTextHeading }}>Our Story: From Kitchen Clutter to Culinary Clarity</Title>
                    <Paragraph style={{ fontSize: '16px', lineHeight: '1.8' }}>
                        Cooknect was born out of frustration with fragmented recipes—notes scribbled on napkins, bookmarks buried in browsers, and ingredients spread across multiple apps. Our founder, a passionate home cook, envisioned a single, beautifully designed place where cooking was effortless. In 2020, we launched **Cooknect** to digitize, organize, and simplify the culinary world.
                    </Paragraph>
                    <Paragraph style={{ fontSize: '16px', lineHeight: '1.8' }}>
                        Today, we are more than just a recipe platform; we are a global community. We blend cutting-edge technology with timeless culinary tradition to provide tools that help you plan, shop, and cook with joy. We believe that the best recipes are the ones you make your own, and we're here to make that possible.
                    </Paragraph>
                    <Button type="link" href="/contact" style={{ paddingLeft: 0, fontWeight: '600' }}>
                        Meet the team (Coming Soon!)
                    </Button>
                </Col>

                {/* Story Image Placeholder */}
                <Col xs={24} lg={10} style={{ display: 'flex', justifyContent: 'center' }}>
                    <Card
                        hoverable
                        style={{ 
                            width: '100%', 
                            maxWidth: '400px',
                            minHeight: '300px',
                            borderRadius: '16px', 
                            backgroundColor: token.colorFillAlter,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            flexDirection: 'column'
                        }}
                        bodyStyle={{ padding: '40px' }}
                    >
                        <BookOpen size={64} style={{ color: token.colorPrimary }} />
                        <Title level={4} style={{ marginTop: '16px' }}>The Digital Cooknect</Title>
                        <Paragraph style={{ textAlign: 'center' }}>
                            A place for every recipe, perfectly organized.
                        </Paragraph>
                    </Card>
                </Col>
            </Row>
        </div>
        
        <Divider style={{ margin: '0 0 80px 0' }} />

        {/* 4. Core Values Section */}
        <div style={{ ...styles.contentSection, paddingTop: 0 }}>
            <Title level={2} style={{ textAlign: 'center', marginBottom: '40px' }}>Our Core Values</Title>
            
            <Row gutter={[24, 24]}>
                {coreValues.map((value, index) => (
                    <Col key={index} xs={24} md={8}>
                        <Card 
                            style={styles.card}
                            hoverable
                            bodyStyle={{ padding: '30px' }}
                        >
                            <div style={{ marginBottom: '16px' }}>
                                {React.cloneElement(value.icon, { color: accentColor })}
                            </div>
                            <Title level={3} style={{ fontSize: '20px', margin: '0 0 10px 0' }}>{value.title}</Title>
                            <Paragraph style={{ color: token.colorTextSecondary }}>
                                {value.description}
                            </Paragraph>
                        </Card>
                    </Col>
                ))}
            </Row>
        </div>

      </Content>

      {/* 5. Footer */}
      <Footer style={styles.footer}>
        Cooknect ©{new Date().getFullYear()} Created with ❤️ by APIBP Team 07
      </Footer>
    </Layout>
  );
};

export default About;