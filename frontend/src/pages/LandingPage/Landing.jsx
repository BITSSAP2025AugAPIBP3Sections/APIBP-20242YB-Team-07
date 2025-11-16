import React from 'react';
import { Layout, Typography, Button, Card, Row, Col, theme, Divider, Carousel, List, Space, Collapse } from 'antd';
import { ChefHat, Feather, Search, Share2, ArrowRight, Quote, BookOpen, Star, Users, Utensils, ClipboardCheck, Heart, Soup } from 'lucide-react';
import NonAuthNavbar from '../../components/Navbar/NonAuthNavbar/NonAuthNavbar';
import { useNavigate } from 'react-router-dom';

const { Content, Footer } = Layout;
const { Title, Paragraph } = Typography;

const features = [
  { icon: <Feather size={32} color="#fff" />, title: 'Digitize Your Legacy', description: 'Easily transcribe your grandmother’s handwritten recipes and keep them safe forever.', color: '#52c41a' },
  { icon: <Search size={32} color="#fff" />, title: 'Smart Discovery', description: 'Find recipes instantly by ingredient, cuisine, or time—never eat the same thing twice.', color: '#faad14' },
  { icon: <Share2 size={32} color="#fff" />, title: 'Share & Collaborate', description: 'Invite family and friends to view, rate, and contribute to your shared cooknects.', color: '#1890ff' },
];

const testimonials = [
  { quote: "Cooknect saved my family traditions. Everything is organized, and the search function is flawless!", author: "Jamie Oliver, Home Chef", rating: 5 },
  { quote: "The best platform for food bloggers and home cooks. Sharing my new creations is easier than ever.", author: "Sonia P., Recipe Creator", rating: 5 },
  { quote: "Absolutely essential for anyone who loves cooking. No more messy papers or lost recipes!", author: "Alex K., Food Enthusiast", rating: 4 },
];

const metrics = [
    { count: '10K+', label: 'Active Users', icon: <Users color="#1890ff" />, color: '#e6f7ff' },
    { count: '500K+', label: 'Recipes Stored', icon: <Utensils color="#52c41a" />, color: '#f6ffed' },
    { count: '4.8/5', label: 'Average Rating', icon: <Star fill="#faad14" color="#faad14" />, color: '#fffbe6' },
    { count: '1.2M', label: 'Meals Cooked', icon: <Heart color="#ff4d4f" />, color: '#fff0f6' },
];

const categories = ['Italian', 'Vegan', 'Desserts', 'Quick Meals', 'Gluten-Free', 'Holiday', 'Asian Fusion', 'Grilling'];


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
    padding: '80px 24px',
    textAlign: 'center',
    background: `linear-gradient(135deg, ${token.colorPrimaryBg} 0%, ${token.colorBgContainer} 100%)`,
    minHeight: '500px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
  }),
  heroTitle: {
    fontSize: 'clamp(36px, 8vw, 52px)',
    fontWeight: '900',
    marginBottom: '16px',
    maxWidth: '800px',
  },
  section: {
    padding: '80px 24px',
    textAlign: 'center',
  },
  cardIconWrapper: (color) => ({
    backgroundColor: color,
    borderRadius: '50%',
    padding: '16px',
    display: 'inline-flex',
    marginBottom: '16px',
    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
  }),
  testimonialCard: {
    maxWidth: '800px',
    margin: '0 auto',
    padding: '20px',
    borderRadius: '16px',
    boxShadow: '0 10px 25px rgba(0, 0, 0, 0.15)',
  },
  deviceMockupCard: {
    padding: 0,
    borderRadius: '20px',
    overflow: 'hidden',
    boxShadow: '0 15px 30px rgba(0, 0, 0, 0.2)',
    transition: 'transform 0.5s ease-out, box-shadow 0.5s ease-out',
    cursor: 'pointer',
    width: '100%',
    maxWidth: '400px',
    margin: '0 auto',
    border: '8px solid #333',
    backgroundColor: '#000',
    position: 'relative',
    aspectRatio: '9 / 16',
  },
  deviceScreen: (token) => ({
    width: '100%',
    height: '100%',
    backgroundColor: token.colorError,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#fff',
    fontSize: '20px',
    fontWeight: '700',
    padding: '20px',
    borderRadius: '10px',
    flexDirection: 'column',
  }),
  metricCard: (color) => ({
    borderRadius: '12px',
    height: '100%',
    backgroundColor: color,
    textAlign: 'left',
    padding: '24px',
  }),
  recipeOfTheDayBanner: {
      padding: '100px 24px',
      backgroundImage: `url(https://placehold.co/1200x400/87d068/ffffff?text=Fresh+Ingredient+Background)`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundAttachment: 'fixed',
      color: '#fff',
      position: 'relative',
      zIndex: 1,
      borderRadius: '20px',
      margin: '40px 0',
      overflow: 'hidden',
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
const TestimonialRating = ({ count }) => {
    const containerStyle = { 
        margin: '16px 0', 
        display: 'flex', 
        justifyContent: 'center' 
    };

    return (
        <Space size={4} style={containerStyle}>
            {Array.from({ length: count }, (_, i) => (
                <Star 
                    key={i} 
                    size={22}
                    fill="#faad14" 
                    stroke="#faad14" 
                    style={{ 
                        cursor: 'pointer',
                        transition: 'transform 0.3s ease-out',
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.3) rotate(5deg)'}
                    onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1) rotate(0deg)'}
                />
            ))}
        </Space>
    );
};

// Component with Hover Animation
const DeviceMockup = ({ token }) => {
    const [isHovered, setIsHovered] = React.useState(false);
    
    const hoverStyle = {
        transform: isHovered ? 'scale(1.05) translateY(-5px)' : 'scale(1)',
        boxShadow: isHovered ? '0 25px 50px rgba(0, 0, 0, 0.3)' : styles.deviceMockupCard.boxShadow,
    };

    return (
        <div 
            style={{ ...styles.deviceMockupCard, ...hoverStyle }}
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
        >
            <div style={styles.deviceScreen(token)}>
                <ClipboardCheck size={48} style={{ marginBottom: '10px' }} />
                Recipe View Mockup
                <small style={{ marginTop: '10px', opacity: 0.8 }}>Hover/Tap to see interaction</small>
            </div>
        </div>
    );
};

const text1 = (
  <p style={{ paddingInlineStart: 24, fontSize: '16px' }}>
    Learn how to upload your own recipes, add ingredient lists, cooking steps, and share them with the community.
  </p>
);

const text2 = (
  <p style={{ paddingInlineStart: 24, fontSize: '16px' }}>
    Our platform automatically calculates calories, macros, and nutritional insights to help you eat smarter.
  </p>
);

const text3 = (
  <p style={{ paddingInlineStart: 24, fontSize: '16px' }}>
    Join weekly cooking challenges, earn badges, and compete with other home chefs for fun rewards.
  </p>
);

const text4 = (
  <p style={{ paddingInlineStart: 24, fontSize: '16px' }}>
    Save your favorite recipes, create collections like “Meal Prep,” “Family Dinners,”  
    or “5-Ingredient Meals,” and access them anytime.
  </p>
);

const text5 = (
  <p style={{ paddingInlineStart: 24, fontSize: '16px' }}>
    Tag ingredients, mark allergens, add dietary categories (Vegan, Keto, Gluten-Free),  
    and help others find the recipes that fit their lifestyle.
  </p>
);

const text6 = (
  <p style={{ paddingInlineStart: 24, fontSize: '16px' }}>
    Plan your meals for the week, auto-generate a shopping list, and keep track  
    of ingredients you already have in your pantry.
  </p>
);

const items = [
  { key: '1', label: <p style={{fontSize:"14px",fontWeight:"800"}}>How to Share Your Recipes</p>, children: text1 },
  { key: '2', label: <p style={{fontSize:"14px",fontWeight:"800"}}>How Nutrition Breakdown Works</p>, children: text2 },
  { key: '3', label: <p style={{fontSize:"14px",fontWeight:"800"}}>Cooking Challenges & Badges</p>, children: text3 },
  { key: '4', label: <p style={{fontSize:"14px",fontWeight:"800"}}>Saving Recipes & Creating Collections</p>, children: text4 },
  { key: '5', label: <p style={{fontSize:"14px",fontWeight:"800"}}>Ingredient Tagging & Dietary Labels</p>, children: text5 },
  { key: '6', label: <p style={{fontSize:"14px",fontWeight:"800"}}>Meal Planning & Pantry Tools</p>, children: text6 },
];


// --- Main Application Component ---
const Landing = () => {
  const { token } = theme.useToken();
  const navigate = useNavigate();

  return (
    <Layout style={styles.layout}>
      {/* 1. Header (Static Nav) */}
      <NonAuthNavbar />

      <Content>
        {/* 2. Hero Section */}
        <div style={styles.heroSection(token)}>
          <Title level={1} style={styles.heroTitle}>
            Your Family's Kitchen, Digitally Perfected.
          </Title>
          <Paragraph style={styles.heroSubtitle}>
            Organize, discover, and share your favorite culinary creations with the ultimate online cooknect.
          </Paragraph>
          <Button
            type="primary"
            size="large"
            icon={<ArrowRight size={20} />}
            iconPosition="end"
            style={{ 
              height: '50px', 
              fontSize: '18px', 
              padding: '0 30px',
              backgroundColor: token.colorError, 
              borderColor: token.colorError,
              fontWeight: '600'
            }}
            onClick={() => navigate('/login')}
          >
            Start Your Cooknect Today
          </Button>
        </div>
        
        {/* 3. Metrics/Stats Section */}
        <div style={{ ...styles.section, padding: '40px 24px', backgroundColor: '#fff' }}>
            <Row gutter={[32, 32]} justify="center" style={{ maxWidth: '1200px', margin: '0 auto' }}>
                {metrics.map((metric, index) => (
                    <Col xs={24} sm={12} lg={6} key={index}>
                        <Card hoverable style={styles.metricCard(metric.color)} bodyStyle={{ padding: '24px' }}>
                            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
                                {metric.icon}
                                <span style={{ marginLeft: '12px', fontSize: '16px', fontWeight: '600', color: token.colorTextSecondary }}>
                                    {metric.label}
                                </span>
                            </div>
                            <Title level={1} style={{ margin: 0, fontSize: '48px', fontWeight: '900', color: token.colorText }}>
                                {metric.count}
                            </Title>
                        </Card>
                    </Col>
                ))}
            </Row>
        </div>

        {/* 4. Recipe Showcase (Animated Mockup) */}
        <div style={{ ...styles.section, backgroundColor: token.colorBgLayout }}>
          <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
            <Row gutter={[48, 48]} align="middle">
                {/* Left Column: Text */}
                <Col xs={24} md={12} style={{ textAlign: 'left' }}>
                    <Title level={2} style={{ color: token.colorTextHeading }}>
                        Capture Every Ingredient, Effortlessly.
                    </Title>
                    <Paragraph style={{ fontSize: '18px', lineHeight: '1.6', marginBottom: '24px' }}>
                        Forget messy binders. Cooknect's intuitive interface lets you quickly input, edit, and categorize recipes, making them searchable and accessible from any device.
                    </Paragraph>
                    <List
                        dataSource={['Mobile-first design', 'Step-by-step cooking mode', 'User cooking challenges', 'Ingredient tagging']}
                        renderItem={item => (
                            <List.Item style={{ padding: '8px 0', border: 'none' }}>
                                <Space>
                                    <ClipboardCheck size={18} color={token.colorPrimary} />
                                    <span style={{ color: token.colorText }}>{item}</span>
                                </Space>
                            </List.Item>
                        )}
                    />
                    <Button type="link" size="large" style={{ marginTop: '20px', paddingLeft: 0, fontWeight: '600' }}>
                        See Demo Features <ArrowRight size={16} />
                    </Button>
                </Col>
                {/* Right Column: Animated Mockup */}
                <Col xs={24} md={12}>
                    <DeviceMockup token={token} />
                </Col>
            </Row>
          </div>
        </div>
        
        {/* 5. Core Features Section */}
        <div style={{ ...styles.section, backgroundColor: '#fff' }}>
          <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
            <Title level={2} style={{ color: token.colorTextHeading, marginBottom: '40px' }}>
              The Power is in the Features
            </Title>
            
            <Row gutter={[32, 32]} justify="center">
              {features.map((feature, index) => (
                <Col xs={24} sm={12} md={8} key={index}>
                  <Card 
                    hoverable
                    style={{ textAlign: 'center', height: '100%', borderRadius: '12px', boxShadow: '0 6px 16px rgba(0, 0, 0, 0.08)', border: '1px solid #e8e8e8', }}
                    bodyStyle={{ padding: '40px 20px' }}
                  >
                    <div style={styles.cardIconWrapper(feature.color)}>
                      {feature.icon}
                    </div>
                    <Title level={3} style={{ marginTop: '0', fontSize: '22px' }}>{feature.title}</Title>
                    <Paragraph style={{ color: token.colorTextSecondary }}>
                      {feature.description}
                    </Paragraph>
                  </Card>
                </Col>
              ))}
            </Row>
          </div>
        </div>

        {/* 6. Interactive Categories Section (Creative Element 3: Dynamic Tags) */}
        <div style={{ ...styles.section, backgroundColor: token.colorBgLayout }}>
            <Title level={2} style={{ color: token.colorTextHeading, marginBottom: '40px' }}>
                Find Your Next Obsession
            </Title>
            <Paragraph style={{ maxWidth: '800px', margin: '0 auto 32px' }}>
                Our recipes are meticulously tagged and organized.
            </Paragraph>
            <div style={{ maxWidth: '1000px', margin: '0 auto', textAlign: 'center' }}>
                <Space size={[16, 16]} wrap>
                    {categories.map((cat) => (
                        <Button 
                            key={cat}
                            type="default"
                            size="large"
                            icon={<Soup size={18} />}
                            style={{ 
                                borderRadius: '30px', 
                                padding: '10px 25px', 
                                fontWeight: '600',
                                backgroundColor: token.colorWhite,
                                borderColor: token.colorBorderSecondary,
                            }}
                            onClick={() => console.log(`Browsing ${cat}`)}
                        >
                            {cat}
                        </Button>
                    ))}
                </Space>
            </div>
        </div>

        {/* 7. Testimonials Carousel Section */}
        <div style={{ ...styles.section, backgroundColor: token.colorPrimaryBg }}>
          <Title level={2} style={{ color: token.colorTextHeading, marginBottom: '40px' }}>
            What Our Cooks Say
          </Title>
          <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
            <Carousel autoplay style={{ borderRadius: '16px' }}>
              {testimonials.map((t, index) => (
                <div key={index} style={{ padding: '40px 0' }}>
                  <Card 
                    style={{
                        ...styles.testimonialCard,
                        transition: 'all 0.4s ease-in-out',
                        transform: 'scale(1)',
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.03)'}
                    onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
                  >
                    <Quote size={40} style={{ color: token.colorPrimary, marginBottom: '16px' }} />
                    <Paragraph style={{ fontSize: '20px', lineHeight: '1.6', fontStyle: 'italic' }}>
                      "{t.quote}"
                    </Paragraph>
                    <TestimonialRating count={t.rating} />
                    <Divider style={{ margin: '16px 0' }} />
                    <Paragraph style={{ fontWeight: '600', fontSize: '16px', margin: 0 }}>
                      {t.author}
                    </Paragraph>
                  </Card>
                </div>
              ))}
            </Carousel>
          </div>
        </div>
        
        {/* 8. Inspiration/Blog Preview Section */}
        <div style={{ ...styles.section, backgroundColor: '#f9f9f9' }}>
          <div style={{ maxWidth: '1600px', margin: '0 auto' }}>
            <Collapse 
              items={items} 
              bordered={false} 
              defaultActiveKey={['1']} 
            />
          </div>
        </div>

        {/* 9. Recipe of the Day Banner (Creative Element 4: Simulated Parallax) */}
        <div style={{ maxWidth: '1200px', margin: '40px auto' }}>
            <div style={styles.recipeOfTheDayBanner}>
                <div style={{ 
                    position: 'absolute', 
                    top: 0, left: 0, right: 0, bottom: 0, 
                    backgroundColor: 'rgba(0,0,0,0.5)', 
                    zIndex: 0, 
                    borderRadius: '20px' 
                }}></div>
                
                <div style={{ position: 'relative', zIndex: 1 }}>
                    <Title level={1} style={{ color: '#fff', fontSize: 'clamp(30px, 6vw, 50px)', fontWeight: '900', textShadow: '0 4px 8px rgba(0, 0, 0, 0.4)', margin: '0 0 16px' }}>
                        Today's Featured Dish: Lemon Ricotta Pasta
                    </Title>
                    <Paragraph style={{ fontSize: '22px', color: '#eee', marginBottom: '40px', textShadow: '0 2px 4px rgba(0, 0, 0, 0.4)' }}>
                        A bright, simple 20-minute meal. Discover the community's highest-rated recipes, curated daily by our experts.
                    </Paragraph>
                    <Button
                        type="primary"
                        size="large"
                        icon={<BookOpen size={20} />}
                        style={{ 
                            height: '55px', 
                            fontSize: '20px', 
                            padding: '0 40px',
                            backgroundColor: '#fff', 
                            color: token.colorTextHeading,
                            fontWeight: '700',
                            boxShadow: '0 8px 15px rgba(0, 0, 0, 0.3)'
                        }}
                        onClick={() => navigate('/login')}
                    >
                        View Full Recipe
                    </Button>
                </div>
            </div>
        </div>


        {/* 10. Final CTA (Call to Action) */}
        <div style={{ ...styles.section, padding: '60px 24px', backgroundColor: token.colorErrorBg, borderTop: `5px solid ${token.colorError}` }}>
             <Title level={2} style={{ color: token.colorError, marginBottom: '16px' }}>
                Stop Searching, Start Cooking.
             </Title>
             <Paragraph style={{ fontSize: '20px', color: token.colorTextSecondary, marginBottom: '40px' }}>
                Join thousands of home cooks who have digitized their kitchen today.
             </Paragraph>
             <Button
                type="primary"
                size="large"
                icon={<ChefHat size={20} />}
                iconPosition="end"
                style={{ 
                    height: '55px', 
                    fontSize: '20px', 
                    padding: '0 40px',
                    backgroundColor: token.colorError, 
                    borderColor: token.colorError,
                    fontWeight: '700'
                }}
                onClick={() => console.log('Final CTA Clicked')}
              >
                Sign Up Now — It's Free!
              </Button>
        </div>
      </Content>

      {/* 11. Footer */}
      <Footer style={styles.footer}>
        Cooknect ©{new Date().getFullYear()} Created with ❤️ by APIBP Team 07
      </Footer>
    </Layout>
  );
};

export default Landing;