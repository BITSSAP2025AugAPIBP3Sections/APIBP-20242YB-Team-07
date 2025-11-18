import React, { useState, useEffect } from 'react';
import { 
  Layout, 
  Typography, 
  Button, 
  Card, 
  Row, 
  Col, 
  theme, 
  Space, 
  Divider, 
  Avatar, 
  Input,
  List,
  Tag,
} from 'antd';
import { 
  Heart, 
  Bookmark, 
  Send, 
  MessageCircle, 
  Globe, 
  Volume2, 
  Pause, 
  Loader2, 
  ThumbsUp, 
  ListOrdered,
  ChefHat
} from 'lucide-react';

const { Content } = Layout;
const { Title, Paragraph, Text } = Typography;
const { TextArea } = Input;

// --- Mock Data (Removed Cook Time/Servings) ---
const mockRecipe = {
  id: 'r-101',
  title: 'Spicy Garlic & Honey Glazed Salmon',
  subtitle: 'A stunning weeknight dish with minimal effort and maximum flavor.',
  originalDescription: "This salmon recipe balances sweet, spicy, and savory flavors perfectly. The glaze caramelizes beautifully, creating a crispy exterior while keeping the fish tender and moist. It's quick enough for a weeknight but impressive enough for company. The subtle kick of chili flakes is mandatory!",
  ingredients: [
    '2 Salmon Fillets (6oz each)',
    '3 Cloves Garlic, minced',
    '1/4 cup Honey (local is best)',
    '2 tbsp Soy Sauce (low sodium)',
    '1 tbsp Rice Vinegar',
    '1 tsp Red Chili Flakes',
    '1 tbsp Olive Oil',
    'Salt and Pepper to taste'
  ],
  steps: [
    'Preheat oven to 400°F (200°C). Line a baking sheet with parchment paper.',
    'In a small bowl, whisk together the garlic, honey, soy sauce, rice vinegar, and chili flakes to make the irresistible glaze.',
    'Season salmon fillets with salt and pepper.',
    'Heat olive oil in an oven-safe skillet and sear the salmon skin-side down for 2 minutes for extra crispiness.',
    'Transfer salmon to the oven. Brush generously with the glaze.',
    'Bake for 12-15 minutes, or until the salmon flakes easily. Brush with remaining glaze halfway through.',
    'Serve immediately over rice or with fresh steamed asparagus for a complete meal.'
  ],
  likes: 452,
  images: [
    'https://placehold.co/1200x500/FF6F61/ffffff?text=Feature+Recipe+Image',
  ],
  tags: ['Fish', 'Dinner', 'Spicy', 'Healthy', 'Gluten-Free Option'],
  author: { name: 'Chef Alex', avatar: 'CA' }
};

const mockComments = [
  { id: 1, user: 'FoodieGuru', text: 'Tried this last night, absolutely phenomenal! The glaze is a game-changer.', avatar: 'FG', timestamp: '3 hours ago' },
  { id: 2, user: 'KitchenNewbie', text: 'Super easy to follow. My family loved it!', avatar: 'KN', timestamp: '1 day ago' },
];

// --- Mock API Constants ---
const apiKey = "";
const apiModel = "gemini-2.5-flash-preview-tts"; // For Text-to-Speech

/**
 * --- CSS Styles (Plain CSS-in-JS for a creative look) ---
 */
const styles = {
  contentArea: {
    padding: '0 24px 64px 24px',
    maxWidth: '1280px',
    margin: '0 auto',
  },
  heroImageContainer: {
    width: '100%',
    aspectRatio: '16 / 5', // Wide aspect ratio for magazine look
    borderRadius: '0 0 24px 24px',
    overflow: 'hidden',
    boxShadow: '0 10px 30px rgba(0, 0, 0, 0.15)',
    marginBottom: '40px',
  },
  mainImage: {
    width: '100%',
    height: '100%',
    objectFit: 'cover',
  },
  sectionCard: {
    borderRadius: '16px',
    padding: '24px',
    marginBottom: '32px',
    border: 'none',
    boxShadow: '0 4px 15px rgba(0, 0, 0, 0.05)',
  },
  // Custom list item style for ingredients/steps
  creativeListItem: (colorBgContainer) => ({
    padding: '12px 0',
    borderBottom: `1px solid ${colorBgContainer}`,
    fontSize: '16px',
    lineHeight: '1.6',
  }),
  // Enhanced description container
  descriptionContainer: {
    padding: '30px',
    borderRadius: '16px',
    backgroundColor: '#fef3e3', // Creamy background
    border: '2px solid #FF6F61',
    boxShadow: '0 6px 15px rgba(255, 111, 97, 0.2)',
  },
  // Animation for loading states
  loadingAnimation: {
    animation: 'spin 1s linear infinite',
    display: 'inline-block',
  },
  commentInputWrapper: {
    display: 'flex',
    gap: '12px',
    marginBottom: '32px',
  }
};

/**
 * Custom hook to handle TTS (Text-to-Speech) API interaction
 * (Logic is reused and robust)
 */
const useAudioPlayer = () => {
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [isLoadingAudio, setIsLoadingAudio] = useState(false);
  const [audio, setAudio] = useState(null);

  const base64ToArrayBuffer = (base64) => {
    const binaryString = window.atob(base64);
    const len = binaryString.length;
    const bytes = new Uint8Array(len);
    for (let i = 0; i < len; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }
    return bytes.buffer;
  };

  const pcmToWav = (pcm16, sampleRate = 24000) => {
    const numChannels = 1;
    const bytesPerSample = 2; // 16-bit PCM
    const blockAlign = numChannels * bytesPerSample;
    const byteRate = sampleRate * blockAlign;
    const dataSize = pcm16.length * bytesPerSample;
    const buffer = new ArrayBuffer(44 + dataSize);
    const view = new DataView(buffer);
    let offset = 0;

    const writeString = (s) => {
      for (let i = 0; i < s.length; i++) {
        view.setUint8(offset + i, s.charCodeAt(i));
      }
      offset += s.length;
    };

    // RIFF header
    writeString('RIFF');
    view.setUint32(offset, 36 + dataSize, true); offset += 4;
    writeString('WAVE');
    
    // fmt sub-chunk
    writeString('fmt ');
    view.setUint32(offset, 16, true); offset += 4; // Sub-chunk size
    view.setUint16(offset, 1, true); offset += 2; // Audio format (1 for PCM)
    view.setUint16(offset, numChannels, true); offset += 2;
    view.setUint32(offset, sampleRate, true); offset += 4;
    view.setUint32(offset, byteRate, true); offset += 4;
    view.setUint16(offset, blockAlign, true); offset += 2;
    view.setUint16(offset, bytesPerSample * 8, true); offset += 2; // Bits per sample

    // data sub-chunk
    writeString('data');
    view.setUint32(offset, dataSize, true); offset += 4;

    // Write PCM data
    for (let i = 0; i < pcm16.length; i++) {
      view.setInt16(offset, pcm16[i], true);
      offset += bytesPerSample;
    }

    return new Blob([view], { type: 'audio/wav' });
  };

  const playAudio = async (text) => {
    if (isSpeaking) {
      if (audio) {
        audio.pause();
        audio.currentTime = 0;
      }
      setIsSpeaking(false);
      return;
    }

    if (audio) {
      // If audio already exists, just replay it
      audio.play();
      setIsSpeaking(true);
      return;
    }

    setIsLoadingAudio(true);
    const apiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${apiModel}:generateContent?key=${apiKey}`;

    const payload = {
        contents: [{
            parts: [{ text: `Read the following recipe description clearly and engagingly: ${text}` }]
        }],
        generationConfig: {
            responseModalities: ["AUDIO"],
            speechConfig: {
                voiceConfig: {
                    prebuiltVoiceConfig: { voiceName: "Kore" }
                }
            }
        },
        model: apiModel
    };

    try {
      let response;
      for (let i = 0; i < 3; i++) { // Max 3 retries
        response = await fetch(apiUrl, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (response.ok) break;
        if (i < 2) await new Promise(resolve => setTimeout(resolve, Math.pow(2, i) * 1000));
        else throw new Error("API request failed after retries.");
      }

      const result = await response.json();
      const part = result?.candidates?.[0]?.content?.parts?.[0];
      const audioData = part?.inlineData?.data;
      const mimeType = part?.inlineData?.mimeType;

      if (audioData && mimeType && mimeType.startsWith("audio/")) {
        const sampleRateMatch = mimeType.match(/rate=(\d+)/);
        const sampleRate = sampleRateMatch ? parseInt(sampleRateMatch[1], 10) : 24000;
        const pcmData = base64ToArrayBuffer(audioData);
        const pcm16 = new Int16Array(pcmData);
        const wavBlob = pcmToWav(pcm16, sampleRate);
        const audioUrl = URL.createObjectURL(wavBlob);
        
        const newAudio = new Audio(audioUrl);
        newAudio.onended = () => setIsSpeaking(false);
        setAudio(newAudio);
        newAudio.play();
        setIsSpeaking(true);

      } else {
        console.error("Failed to get audio data from API response.");
        alert("Sorry, failed to generate audio. Please check the console.");
      }
    } catch (error) {
      console.error('Error generating audio:', error);
      alert('An error occurred during audio generation.');
    } finally {
      setIsLoadingAudio(false);
    }
  };

  return { isSpeaking, isLoadingAudio, playAudio };
};

/**
 * Main Recipe Page Component
 */
const Recipe = () => {
  const { token } = theme.useToken();
  const [likes, setLikes] = useState(mockRecipe.likes);
  const [isLiked, setIsLiked] = useState(false);
  const [isSaved, setIsSaved] = useState(false);
  const [comments, setComments] = useState(mockComments);
  const [commentText, setCommentText] = useState('');

  // Translation State
  const [isTranslating, setIsTranslating] = useState(false);
  const [isTranslated, setIsTranslated] = useState(false);
  const [displayDescription, setDisplayDescription] = useState(mockRecipe.originalDescription);
  
  // Custom hook for TTS functionality
  const { isSpeaking, isLoadingAudio, playAudio } = useAudioPlayer();

  // --- Handlers ---

  const handleLike = () => {
    setIsLiked(!isLiked);
    setLikes(isLiked ? likes - 1 : likes + 1);
  };

  const handleSave = () => {
    setIsSaved(!isSaved);
  };

  const handleCommentSubmit = () => {
    if (!commentText.trim()) return;

    const newComment = {
      id: Date.now(),
      user: 'CurrentUser', // Mock current user
      text: commentText.trim(),
      avatar: 'CU',
      timestamp: 'Just now'
    };
    setComments([newComment, ...comments]);
    setCommentText('');
  };

  const handleTranslate = async () => {
    if (isTranslated) {
      // Revert to original text
      setDisplayDescription(mockRecipe.originalDescription);
      setIsTranslated(false);
      return;
    }

    setIsTranslating(true);
    
    // --- Mocking the Translation API Call (To be replaced by your gemini-2.5-flash-preview-09-2025 call) ---
    const mockTranslation = "Cette recette de saumon est l'équilibre parfait entre les saveurs sucrées, épicées et salées. La glace caramélise magnifiquement, créant un extérieur croustillant tout en gardant le poisson tendre et moelleux.";
    
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 1500)); 

    setDisplayDescription(mockTranslation);
    setIsTranslated(true);
    setIsTranslating(false);
  };

  const handleAudio = () => {
    playAudio(displayDescription);
  };
  
  return (
    <Layout style={{ minHeight: '100vh', backgroundColor: token.colorBgContainer }}>
      {/* Hero Image Section */}
      <div style={styles.heroImageContainer}>
        <img 
          src={mockRecipe.images[0]} 
          alt={mockRecipe.title} 
          style={styles.mainImage}
          onError={(e) => { e.target.onerror = null; e.target.src="https://placehold.co/1200x500/FF6F61/ffffff?text=Image+Unavailable"; }}
        />
      </div>

      <Content>
        <div style={styles.contentArea}>
          
          <Row gutter={[40, 40]}>
            
            {/* --- LEFT COLUMN: Title, Description, Ingredients, Steps --- */}
            <Col xs={24} lg={16}>
              
              {/* Recipe Title & Subtitle */}
              <div style={{ marginBottom: '24px' }}>
                <Title level={1} style={{ fontWeight: '800', margin: 0 }}>
                  {mockRecipe.title}
                </Title>
                <Paragraph type="secondary" style={{ fontSize: '1.2em' }}>
                  {mockRecipe.subtitle}
                </Paragraph>
                <div style={{ marginTop: '16px' }}>
                  <Avatar style={{ backgroundColor: '#264653', marginRight: '8px' }}>{mockRecipe.author.avatar}</Avatar>
                  <Text strong>{mockRecipe.author.name}</Text>
                </div>
              </div>

              {/* Action Buttons (Likes, Saves, Tags) */}
              <Card style={{ marginBottom: '32px', borderRadius: '16px', backgroundColor: token.colorFillAlter }} bordered={false}>
                <Space size="large" wrap>
                  <Button
                    size="large"
                    type={isLiked ? 'primary' : 'default'}
                    danger={isLiked}
                    icon={<Heart size={20} fill={isLiked ? 'white' : 'red'} />}
                    onClick={handleLike}
                  >
                    {likes} Loves
                  </Button>
                  <Button
                    size="large"
                    type={isSaved ? 'default' : 'dashed'}
                    icon={<Bookmark size={20} fill={isSaved ? token.colorPrimary : 'none'} />}
                    onClick={handleSave}
                  >
                    {isSaved ? 'Bookmarked' : 'Save Recipe'}
                  </Button>
                </Space>
                <Divider style={{ margin: '16px 0' }} />
                <Space size={[0, 8]} wrap>
                  <Text strong style={{ marginRight: '8px' }}>Tags:</Text>
                  {mockRecipe.tags.map(tag => (
                    <Tag key={tag} color="#264653" style={{ borderRadius: '12px', padding: '4px 12px' }}>
                      {tag}
                    </Tag>
                  ))}
                </Space>
              </Card>

              {/* Description & Feature Controls (Translate & Audio) */}
              <div style={{ marginBottom: '40px' }}>
                <Title level={2} style={{ color: token.colorPrimary, marginBottom: '16px', fontWeight: '700' }}>
                  The Story
                </Title>
                
                <Space style={{ marginBottom: '20px' }} wrap>
                  <Button 
                    size="large"
                    icon={isTranslating ? <Loader2 size={18} style={styles.loadingAnimation} /> : <Globe size={18} />} 
                    onClick={handleTranslate}
                    disabled={isTranslating || isLoadingAudio}
                    type={isTranslated ? 'primary' : 'default'}
                  >
                    {isTranslating ? 'Translating...' : (isTranslated ? 'Show Original (EN)' : 'Translate to French')}
                  </Button>
                  <Button 
                    size="large"
                    icon={isLoadingAudio ? <Loader2 size={18} style={styles.loadingAnimation} /> : (isSpeaking ? <Pause size={18} /> : <Volume2 size={18} />)} 
                    onClick={handleAudio}
                    disabled={isLoadingAudio || isTranslating}
                    type={isSpeaking ? 'primary' : 'default'}
                    style={{ backgroundColor: isSpeaking ? '#52c41a' : undefined, borderColor: isSpeaking ? '#52c41a' : undefined }}
                  >
                    {isLoadingAudio ? 'Loading Audio...' : (isSpeaking ? 'Pause Reading' : 'Read Aloud')}
                  </Button>
                </Space>

                <div style={styles.descriptionContainer}>
                  <Paragraph style={{ fontSize: '1.1em', lineHeight: '1.8', margin: 0 }}>
                    {displayDescription}
                  </Paragraph>
                </div>
              </div>

              {/* Ingredients List */}
              <div style={styles.sectionCard}>
                <Title level={2} style={{ color: '#264653', marginBottom: '24px', fontWeight: '700' }}>
                  <ChefHat size={24} style={{ marginRight: '8px' }} />
                  What You Need
                </Title>
                <List
                  itemLayout="horizontal"
                  dataSource={mockRecipe.ingredients}
                  renderItem={(item) => (
                    <List.Item style={styles.creativeListItem(token.colorBorderSecondary)}>
                      <Text strong style={{ color: token.colorPrimary }}>-</Text>
                      <Text style={{ marginLeft: '12px' }}>{item}</Text>
                    </List.Item>
                  )}
                />
              </div>

              {/* Steps */}
              <div style={styles.sectionCard}>
                <Title level={2} style={{ color: '#264653', marginBottom: '24px', fontWeight: '700' }}>
                  <ListOrdered size={24} style={{ marginRight: '8px' }} />
                  How To Make It
                </Title>
                <List
                  itemLayout="horizontal"
                  dataSource={mockRecipe.steps}
                  renderItem={(item, index) => (
                    <List.Item style={styles.creativeListItem(token.colorBorderSecondary)}>
                      <Space align="start">
                        <Avatar size="default" style={{ backgroundColor: token.colorPrimary, color: 'white', fontWeight: 'bold' }}>{index + 1}</Avatar>
                        <Paragraph style={{ marginBottom: 0, fontSize: '1em' }}>{item}</Paragraph>
                      </Space>
                    </List.Item>
                  )}
                />
              </div>

            </Col>
            
            {/* --- RIGHT COLUMN: Secondary Image & Comments --- */}
            <Col xs={24} lg={8}>

              {/* Empty placeholder/ad space (or could be for related recipes) */}
              <div style={{ 
                height: '300px', 
                borderRadius: '16px', 
                backgroundColor: token.colorFillAlter, 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center', 
                marginBottom: '40px',
                border: `2px dashed ${token.colorBorder}`
              }}>
                <Text type="secondary">Chef's Tip or Related Recipes Go Here</Text>
              </div>

              {/* Comments Section */}
              <div style={{ padding: '0 10px' }}>
                <Title level={2} style={{ color: token.colorText, marginBottom: '24px', fontWeight: '700' }}>
                  <MessageCircle size={24} style={{ marginRight: '8px' }} />
                  Community Feedback ({comments.length})
                </Title>

                {/* Comment Input */}
                <div style={styles.commentInputWrapper}>
                  <Avatar style={{ backgroundColor: token.colorPrimary }}>CU</Avatar>
                  <TextArea
                    rows={2}
                    placeholder="Leave a helpful comment or question..."
                    value={commentText}
                    onChange={(e) => setCommentText(e.target.value)}
                    style={{ flexGrow: 1, borderRadius: '8px' }}
                  />
                  <Button 
                    type="primary" 
                    icon={<Send size={18} />} 
                    style={{ height: 'auto', padding: '10px 15px', alignSelf: 'flex-end' }}
                    onClick={handleCommentSubmit}
                    disabled={!commentText.trim()}
                  >
                    Post
                  </Button>
                </div>

                {/* Comment List */}
                <List
                  itemLayout="horizontal"
                  dataSource={comments}
                  renderItem={(item) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={<Avatar style={{ backgroundColor: token.colorSuccess }}>{item.avatar}</Avatar>}
                        title={
                          <Space size={4}>
                            <Text strong>{item.user}</Text> 
                            <ThumbsUp size={14} color="#52c41a" style={{ marginLeft: 4 }}/>
                          </Space>
                        }
                        description={
                          <>
                            <Paragraph style={{ marginBottom: 4 }}>{item.text}</Paragraph>
                            <Text type="secondary" style={{ fontSize: '0.8em' }}>{item.timestamp}</Text>
                          </>
                        }
                      />
                    </List.Item>
                  )}
                />
              </div>
              
            </Col>
            
          </Row>
          
        </div>
      </Content>

      {/* Global CSS for spinner animation */}
      <style>
        {`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        
        .ant-layout {
          font-family: 'Inter', sans-serif;
        }

        /* Responsive adjustments for the hero image */
        @media (max-width: 991px) {
          .ant-layout-content > div:first-child {
            padding-top: 24px;
          }
          ${styles.heroImageContainer} {
            aspect-ratio: 4/3;
            border-radius: 16px;
            margin-bottom: 24px;
          }
        }
        `}
      </style>
      
    </Layout>
  );
};

export default Recipe;