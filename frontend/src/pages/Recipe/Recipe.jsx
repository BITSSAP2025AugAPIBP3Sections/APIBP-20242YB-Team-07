import { useState, useEffect } from "react";
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
  Dropdown,
  Select,
} from "antd";
import {
  Heart,
  Bookmark,
  Send,
  MessageCircle,
  ListOrdered,
  ChefHat,
  Delete,
} from "lucide-react";
import { SoundOutlined } from "@ant-design/icons";
import { useParams } from "react-router-dom";
import LoggedInNavbar from "../../components/Navbar/LoggedInNavbar/LoggedInNavbar";
import { useAuth } from "../../auth/AuthContext";

const { Content } = Layout;
const { Title, Paragraph, Text } = Typography;
const { TextArea } = Input;

// * --- CSS Styles (Plain CSS-in-JS for a creative look) ---
const styles = {
  contentArea: {
    padding: "0 24px 64px 24px",
    maxWidth: "1280px",
    margin: "0 auto",
  },
  heroImageContainer: {
    width: "100%",
    aspectRatio: "16 / 5", // Wide aspect ratio for magazine look
    borderRadius: "0 0 24px 24px",
    overflow: "hidden",
    boxShadow: "0 10px 30px rgba(0, 0, 0, 0.15)",
    marginBottom: "40px",
  },
  mainImage: {
    width: "100%",
    height: "100%",
    objectFit: "cover",
  },
  sectionCard: {
    borderRadius: "16px",
    padding: "24px",
    marginBottom: "32px",
    border: "none",
    boxShadow: "0 4px 15px rgba(0, 0, 0, 0.05)",
  },
  // Custom list item style for ingredients/steps
  creativeListItem: (colorBgContainer) => ({
    padding: "12px 0",
    borderBottom: `1px solid ${colorBgContainer}`,
    fontSize: "16px",
    lineHeight: "1.6",
  }),
  // Enhanced description container
  descriptionContainer: {
    padding: "30px",
    borderRadius: "16px",
    backgroundColor: "#fef3e3", // Creamy background
    border: "2px solid #FF6F61",
    boxShadow: "0 6px 15px rgba(255, 111, 97, 0.2)",
  },
  // Animation for loading states
  loadingAnimation: {
    animation: "spin 1s linear infinite",
    display: "inline-block",
  },
  commentInputWrapper: {
    display: "flex",
    gap: "12px",
    marginBottom: "32px",
  },
};

const Recipe = () => {
  const { id: recipeId } = useParams();
  const { token } = theme.useToken();
  const [commentText, setCommentText] = useState("");
  const [recipeData, setRecipeData] = useState(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [audioLoading, setAudioLoading] = useState(false);
  const [deleteButtonLoading, setDeleteButtonLoading] = useState(false);
  const [language, setLanguage] = useState("English");
  const { user } = useAuth();

  useEffect(() => {
    fetchRecipeData();
  }, [recipeId]);

  const fetchRecipeData = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8089/api/v1/recipes/${recipeId}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to fetch recipe data");
      }

      const data = await response.json();
      setRecipeData(data);
    } catch (error) {
      console.error("Error fetching recipe data:", error);
    }
  };

  const likeAndUnlikeRecipe = async () => {
    try {
      const response = await fetch(
        `http://localhost:8089/api/v1/recipes/${recipeId}/like`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
      }
      if (!response.ok) {
        throw new Error("Failed to like/unlike recipe");
      }
      // Refresh recipes after liking/unliking
      fetchRecipeData();
    } catch (error) {
      console.error("Error liking/unliking recipe:", error);
    }
  };

  const handleCommentSubmit = async () => {
    try {
      const response = await fetch(
        `http://localhost:8089/api/v1/recipes/${recipeId}/comments`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
          body: JSON.stringify({ text: commentText }),
        }
      );

      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
        return;
      }

      if (!response.ok) {
        throw new Error("Failed to post comment");
      }

      setCommentText("");

      // Now fetch recipe data after comment is posted
      await fetchRecipeData();
    } catch (error) {
      console.error("Error posting comment:", error);
    }
  };

  const handleDeleteRecipe = async (recipeId) => {
    setDeleteButtonLoading(true);
    try {
      const response = await fetch(
        `http://localhost:8089/api/v1/recipes/${recipeId}`,
        {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      if (response.status === 204) {
        window.location.reload();
      } else {
        throw new Error("Failed to delete recipe");
      }
    } catch (error) {
      console.error("Error deleting recipe:", error);
    } finally {
      setDeleteButtonLoading(false);
    }
  };

  const [currentAudio, setCurrentAudio] = useState(null); // Store audio instance

  const playRecipeAudio = async (recipeId) => {
    setAudioLoading(true);
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8089/api/v1/recipes/${recipeId}/translate/${language}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to fetch audio");
      }

      const contentType = response.headers.get("content-type");
      const audioBlob = await response.blob();
      const audioFile = new Blob([audioBlob], { type: "audio/mpeg" });
      const audioUrl = URL.createObjectURL(audioFile);

      const audio = new Audio(audioUrl);
      setCurrentAudio(audio); // Store audio instance

      audio.onplay = () => setIsPlaying(true);
      audio.onended = () => {
        setIsPlaying(false);
        setCurrentAudio(null);
        URL.revokeObjectURL(audioUrl);
      };
      audio.onerror = (e) => {
        setIsPlaying(false);
        setCurrentAudio(null);
        alert(
          "Failed to play audio. The file may be corrupted or in an unsupported format."
        );
      };

      await audio.play();
    } catch (error) {
      console.error("Error fetching audio:", error);
      alert("Failed to load audio. Please try again.");
    } finally {
      setAudioLoading(false);
    }
  };

  const pauseRecipeAudio = () => {
    if (currentAudio) {
      currentAudio.pause();
      setIsPlaying(false);
    }
  };

  const handleSave = async () => {
    try {
      const response = await fetch(
        `http://localhost:8089/api/v1/recipes/${recipeId}/save`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
      }
      if (!response.ok) {
        throw new Error("Failed to save/unsave recipe");
      }
      // Refresh recipes after saving/unsaving
      fetchRecipeData();
    } catch (error) {
      console.error("Error saving/unsaving recipe:", error);
    }
  };

  return (
    <Layout
      style={{ minHeight: "100vh", backgroundColor: token.colorBgContainer }}
    >
      <LoggedInNavbar activeKey="1" />

      {/* Hero Image Section */}
      <div style={styles.heroImageContainer}>
        <img
          src={recipeData?.recipeImageUrl}
          alt={recipeData?.title}
          style={styles.mainImage}
          onError={(e) => {
            e.target.onerror = null;
            e.target.src =
              "https://placehold.co/1200x500/FF6F61/ffffff?text=Image+Unavailable";
          }}
        />
      </div>

      <Content>
        <div style={styles.contentArea}>
          <Row gutter={[40, 40]}>
            {/* --- LEFT COLUMN: Title, Description, Ingredients, Steps --- */}
            <Col xs={24} lg={16}>
              {/* Recipe Title & Subtitle */}
              <div style={{ marginBottom: "24px" }}>
                <Title level={1} style={{ fontWeight: "800", margin: 0 }}>
                  {recipeData?.title}
                </Title>
                <Paragraph type="secondary" style={{ fontSize: "1.2em" }}>
                  {recipeData?.description}
                </Paragraph>
                <div style={{ marginTop: "16px" }}>
                  <Avatar
                    style={{ backgroundColor: "#264653", marginRight: "8px" }}
                  >
                    {recipeData?.username?.charAt(0).toUpperCase() || "U"}
                  </Avatar>
                  <Text strong>{recipeData?.username}</Text>
                </div>
              </div>

              {/* Action Buttons (Likes, Saves, Tags) */}
              <Card
                style={{
                  marginBottom: "32px",
                  borderRadius: "16px",
                  backgroundColor: token.colorFillAlter,
                }}
                bordered={false}
              >
                <Space size="large" wrap>
                  <Button
                    size="large"
                    type={
                      recipeData?.likedByUser === false ? "primary" : "default"
                    }
                    danger={recipeData?.likedByUser === false ? false : true}
                    icon={
                      <Heart
                        size={20}
                        fill={
                          recipeData?.likedByUser === false ? "white" : "red"
                        }
                      />
                    }
                    onClick={likeAndUnlikeRecipe}
                  >
                    {recipeData?.likesCount} Likes
                  </Button>
                  <Button
                    size="large"
                    type={
                      recipeData?.savedByUser === true ? "default" : "dashed"
                    }
                    icon={
                      <Bookmark
                        size={20}
                        fill={
                          recipeData?.savedByUser === true
                            ? token.colorPrimary
                            : "none"
                        }
                      />
                    }
                    onClick={handleSave}
                  >
                    {recipeData?.savedByUser === true
                      ? "Bookmarked"
                      : "Save Recipe"}
                  </Button>
                  {user?.userData?.id === recipeData?.userId && (
                    <Button
                      size="large"
                      type="primary"
                      danger
                      icon={<Delete size={20} />}
                      onClick={() => {
                        handleDeleteRecipe(recipeData?.id);
                      }}
                      loading={deleteButtonLoading}
                    >
                      Delete Recipe
                    </Button>
                  )}
                </Space>
                <Divider style={{ margin: "16px 0" }} />
                <Space size={[0, 8]} wrap>
                  <Text strong style={{ marginRight: "8px" }}>
                    Cuisine:
                  </Text>
                  <Tag
                    key={recipeData?.cuisine}
                    color="#264653"
                    style={{ borderRadius: "12px", padding: "4px 12px" }}
                  >
                    {recipeData?.cuisine}
                  </Tag>
                </Space>
              </Card>

              {/* Description & Feature Controls (Translate & Audio) */}
              <div style={{ marginBottom: "40px" }}>
                <Title
                  level={2}
                  style={{
                    color: token.colorPrimary,
                    marginBottom: "16px",
                    fontWeight: "700",
                  }}
                >
                  The Story
                </Title>

                <Space style={{ marginBottom: "20px" }} wrap>
                  {!isPlaying ? (
                    <Button
                      type="primary"
                      icon={<SoundOutlined />}
                      loading={audioLoading}
                      onClick={() => playRecipeAudio(recipeData?.id)}
                    >
                      Listen to Recipe
                    </Button>
                  ) : (
                    <Button
                      type="default"
                      danger
                      icon={<SoundOutlined />}
                      onClick={pauseRecipeAudio}
                    >
                      Pause Audio
                    </Button>
                  )}
                  <Select
                    placeholder="Translate Description"
                    style={{ width: 220 }}
                    onChange={(value) => {
                      setLanguage(value);
                    }}
                  >
                    <Select.Option value="Hindi">Hindi</Select.Option>
                    <Select.Option value="English">English</Select.Option>
                    <Select.Option value="Gujarati">Gujarati</Select.Option>
                    <Select.Option value="Bengali">Bengali</Select.Option>
                    <Select.Option value="Marathi">Marathi</Select.Option>
                    <Select.Option value="Kannada">Kannada</Select.Option>
                    <Select.Option value="Telugu">Telugu</Select.Option>
                  </Select>
                </Space>
                <div style={styles.descriptionContainer}>
                  <Paragraph
                    style={{ fontSize: "1.1em", lineHeight: "1.8", margin: 0 }}
                  >
                    {recipeData?.description}
                  </Paragraph>
                </div>
              </div>

              {/* Ingredients & Preparation Steps */}
              <div style={styles.sectionCard}>
                <Title
                  level={2}
                  style={{
                    color: "#264653",
                    marginBottom: "24px",
                    fontWeight: "700",
                  }}
                >
                  <ChefHat size={24} style={{ marginRight: "8px" }} />
                  What You Need
                </Title>
                <List
                  itemLayout="horizontal"
                  dataSource={recipeData?.ingredients || []}
                  renderItem={(item) => (
                    <List.Item
                      style={styles.creativeListItem(
                        token.colorBorderSecondary
                      )}
                    >
                      <Text strong style={{ color: token.colorPrimary }}>
                        -
                      </Text>
                      <Text style={{ marginLeft: "12px" }}>
                        {item.name} {item.quantity && `: ${item.quantity}`}
                      </Text>
                    </List.Item>
                  )}
                />
              </div>

              {/* Preparation Steps */}
              <div style={styles.sectionCard}>
                <Title
                  level={2}
                  style={{
                    color: "#264653",
                    marginBottom: "24px",
                    fontWeight: "700",
                  }}
                >
                  <ListOrdered size={24} style={{ marginRight: "8px" }} />
                  How To Make It
                </Title>
                <List
                  itemLayout="horizontal"
                  dataSource={recipeData?.preparation || []}
                  renderItem={(item, index) => (
                    <List.Item
                      style={styles.creativeListItem(
                        token.colorBorderSecondary
                      )}
                    >
                      <Space align="start">
                        <Avatar
                          size="default"
                          style={{
                            backgroundColor: token.colorPrimary,
                            color: "white",
                            fontWeight: "bold",
                          }}
                        >
                          {index + 1}
                        </Avatar>
                        <Paragraph style={{ marginBottom: 0, fontSize: "1em" }}>
                          {item.step}
                        </Paragraph>
                      </Space>
                    </List.Item>
                  )}
                />
              </div>

              {/* Credits to the Chef */}
              {recipeData?.isTribute && (
                <div style={styles.sectionCard}>
                  <Title
                    level={2}
                    style={{
                      color: token.colorPrimary,
                      marginBottom: "16px",
                      fontWeight: "700",
                    }}
                  >
                    About the Chef
                  </Title>
                  <Space align="center" size="large">
                    <Avatar
                      size={64}
                      style={{ fontSize: "24px" }}
                      imgProps={{ crossOrigin: "anonymous" }}
                      src={recipeData?.tributeImageUrl}
                    />
                    <div>
                      <Text strong style={{ fontSize: "1.2em" }}>
                        {recipeData?.authorName}
                      </Text>
                      <Paragraph
                        style={{ marginTop: "8px", maxWidth: "300px" }}
                      >
                        {recipeData?.tributeDescription ||
                          "This chef prefers to keep an air of mystery about them."}
                      </Paragraph>
                    </div>
                  </Space>
                </div>
              )}
            </Col>

            {/* --- RIGHT COLUMN: Comments Section --- */}
            <Col xs={24} lg={8}>
              {/* Comments Section */}
              <div style={{ padding: "0 10px" }}>
                <Title
                  level={2}
                  style={{
                    color: token.colorText,
                    marginBottom: "24px",
                    fontWeight: "700",
                  }}
                >
                  <MessageCircle size={24} style={{ marginRight: "8px" }} />
                  Community Feedback
                </Title>

                {/* Comment Input */}
                <div style={styles.commentInputWrapper}>
                  <TextArea
                    rows={2}
                    placeholder="Leave a helpful comment or question..."
                    value={commentText}
                    onChange={(e) => setCommentText(e.target.value)}
                    style={{ flexGrow: 1, borderRadius: "8px" }}
                  />
                  <Button
                    type="primary"
                    icon={<Send size={18} />}
                    style={{
                      height: "auto",
                      padding: "10px 15px",
                      alignSelf: "flex-end",
                    }}
                    onClick={handleCommentSubmit}
                    disabled={!commentText.trim()}
                  >
                    Post
                  </Button>
                </div>

                {/* Comment List */}
                <List
                  itemLayout="horizontal"
                  dataSource={recipeData?.comments || []}
                  renderItem={(item) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={
                          <Avatar
                            style={{ backgroundColor: token.colorSuccess }}
                          >
                            {/* default avatar of user */}
                            {item?.author?.charAt(0).toUpperCase() || "U"}
                          </Avatar>
                        }
                        title={
                          <Space size={4}>
                            <Text strong>{item.author}</Text>
                          </Space>
                        }
                        description={
                          <>
                            <Paragraph style={{ marginBottom: 4 }}>
                              {item.text}
                            </Paragraph>
                            <Text
                              type="secondary"
                              style={{ fontSize: "0.8em" }}
                            >
                              {item.timestamp}
                            </Text>
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
