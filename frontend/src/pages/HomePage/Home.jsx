import "./Home.css";
import LoggedInNavbar from "../../components/Navbar/LoggedInNavbar/LoggedInNavbar";
import { useNavigate } from "react-router-dom";

import {
  Modal,
  Form,
  Input,
  Button,
  Space,
  Typography,
  Card,
  theme,
  Tooltip,
  Divider,
  Avatar,
  Tag,
} from "antd";
import {
  HeartOutlined,
  MessageFilled,
  ShareAltOutlined,
} from "@ant-design/icons";
import {
  Plus,
  MinusCircle,
  Send,
  ChefHat,
  ListOrdered,
  BookOpen,
  Upload,
} from "lucide-react";
import { useEffect, useState } from "react";

const { TextArea } = Input;
const { Title, Text } = Typography;

const submitRecipe = (values) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      console.log("--- Recipe Data to be Saved ---");
      console.log(values);
      console.log("-------------------------------");
      resolve({ success: true });
    }, 1500);
  });
};

const Home = () => {
  const { token } = theme.useToken();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const [userProfile, setUserProfile] = useState({
    name: "",
    bio: "",
    avatarUrl: "",
    id: "",
  });

  const formItemStyle = {
    marginBottom: 20,
  };
  const headerStyle = {
    backgroundColor: token.colorPrimaryBg,
    padding: "12px 20px",
    borderRadius: "8px 8px 0 0",
    display: "flex",
    alignItems: "center",
    gap: "12px",
  };

  useEffect(() => {
    fetchUserProfile();
  }, []);

  const fetchUserProfile = async () => {
    try {
      const response = await fetch(
        "http://localhost:8089/api/v1/users/user-details",
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      if (!response.ok) {
        throw new Error("Failed to fetch user profile");
      }
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
      }
      const data = await response.json();
      setUserProfile({
        name: data.username,
        bio: data.bio,
        avatarUrl: data.avatarUrl,
        id: data.id,
      });
    } catch (error) {
      console.error("Error fetching user profile:", error);
    }
  };

  const handleCancel = () => {
    setIsModalVisible(false);
    form.resetFields();
  };

  const onFinish = async (values) => {
    setLoading(true);
    try {
      // NOTE: Replace alert() with a custom Antd notification or Modal
      await submitRecipe(values);
      console.log(
        "Recipe posted successfully! Check the console for the mock data."
      );
      handleCancel();
    } catch (error) {
      console.error("Submission error:", error);
      // NOTE: Replace alert() with a custom Antd notification or Modal
      console.log("Failed to post recipe. See console for details.");
    } finally {
      setLoading(false);
    }
  };
  //   console the form data
  console.log("Form Data:", form.getFieldsValue());

  return (
    <>
      <LoggedInNavbar activeKey="1" />
      <div className="main-content">
        <div className="main-grid">
          {/* ----------------------------Left Bar---------------------------------- */}
          <div className="left-sidebar-column">
            <div className="profile-card">
              <div className="profile-header"></div>
              <div className="profile-content">
                <div>
                  {
                    <Avatar
                      className="profile-avatar"
                      src={userProfile.avatarUrl}
                    />
                  }
                </div>
                <div className="profile-name">{userProfile.name}</div>
                <div className="profile-title">{userProfile.bio}</div>

                <div className="profile-stats">
                  <div className="stat-item">
                    <span className="stat-number">125</span>
                    <span className="stat-label">Recipes Posted</span>
                  </div>
                  <div className="stat-item">
                    <span className="stat-number">2.3K</span>
                    <span className="stat-label">Recipe Saved</span>
                  </div>
                </div>

                <a
                  href="#"
                  className="profile-link"
                  onClick={() => navigate(`/profile/${userProfile.id}`)}
                >
                  View Profile
                </a>
                <a
                  href="#"
                  className="profile-link"
                  onclick="navigate('My Saved Recipes'); return false;"
                >
                  <i class="fas fa-bookmark"></i> My Cookbook
                </a>
              </div>
            </div>
          </div>

          {/* ----------------------------Recipe Card------------------------------- */}
          <div className="feed-column">
            <div className="feed-controls">
              <div className="search-container">
                <input
                  type="text"
                  placeholder="Search recipes, users, or tags..."
                  className="search-bar"
                  onfocus="showMessage('Mock search activated.');"
                />
              </div>
              <button
                className="feed-post-btn"
                onClick={() => setIsModalVisible(true)}
              >
                <Upload /> Share Recipe
              </button>
            </div>

            <h2>Latest Community Recipes</h2>

            <div className="recipe-card" data-id="1">
              <img
                src="https://placehold.co/800x600/ff7f50/ffffff?text=Vibrant+Salmon+Recipe"
                onerror="this.onerror=null; this.src='https://placehold.co/800x600/ccc/333?text=Image+Load+Failed';"
                alt="Spicy Sriracha Salmon Bowls"
                className="recipe-image"
              />
              <div className="card-content">
                <div className="card-author-info">
                  <div className="author-avatar">CA</div>
                  <div>
                    <h3>Spicy Sriracha Salmon Bowls</h3>
                    <div className="author-name">
                      Posted by{" "}
                      <a href="#" onclick="navigate('Profile'); return false;">
                        @Chef_Alex
                      </a>
                    </div>
                  </div>
                </div>
                <p className="recipe-description">
                  A quick and delicious weeknight dinner with a fiery kick.
                  Ready in under 30 minutes!
                </p>
                <div>
                  <Tag color="#f50">orange</Tag>
                </div>
                <div className="card-divider"></div>
                <div className="card-actions">
                  <button
                    className="action-button like-button"
                    onclick="likeRecipe(1, this)"
                  >
                    <HeartOutlined />
                    <span id="like-count-1">154</span>
                  </button>
                  <button
                    className="action-button"
                    onclick="navigate('View Comments');"
                  >
                    <MessageFilled />
                    28
                  </button>
                  <button
                    className="action-button"
                    onclick="navigate('Share');"
                  >
                    <ShareAltOutlined />
                    Share
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* -----------------------------Challenge Column------------------------- */}
          <div class="right-sidebar-column">
            <div className="sidebar-card challenge-card">
              <h4>
                <i class="fas fa-fire"></i> Cooking Challenges
              </h4>
              <ul className="challenge-list">
                <li className="challenge-item">
                  <div className="item-details">
                    <div className="icon">
                      <i class="fas fa-award"></i>
                    </div>
                    <div>
                      <div className="item-title">
                        The Perfect Pasta Challenge
                      </div>
                      <div className="item-meta">Theme: Comfort Food</div>
                    </div>
                  </div>
                  <button
                    className="join-btn"
                    onclick="showMessage('Joined Perfect Pasta Challenge!');"
                  >
                    Join
                  </button>
                </li>
                <li className="challenge-item">
                  <div className="item-details">
                    <div className="icon">
                      <i class="fas fa-leaf"></i>
                    </div>
                    <div>
                      <div className="item-title">Veganuary: Green Cuisine</div>
                      <div className="item-meta">Theme: Plant-Based</div>
                    </div>
                  </div>
                  <button
                    className="join-btn"
                    onclick="showMessage('Joined Veganuary Challenge!');"
                  >
                    Join
                  </button>
                </li>
              </ul>
              <a
                href="#"
                className="view-all"
                onclick="navigate('View All Challenges'); return false;"
              >
                View All Challenges
              </a>
            </div>

            <div className="sidebar-card winner-card">
              <h4>
                <i class="fas fa-trophy"></i> Recent Winners
              </h4>
              <ul className="winner-list">
                <li className="winner-item">
                  <div className="item-details">
                    <div className="winner-avatar">GG</div>
                    <div>
                      <div className="item-title">@GourmetGuru</div>
                      <div className="item-meta">Winner of: Holiday Roast</div>
                    </div>
                  </div>
                </li>
                <li className="winner-item">
                  <div className="item-details">
                    <div className="winner-avatar">BB</div>
                    <div>
                      <div className="item-title">@BakeBoss</div>
                      <div className="item-meta">Winner of: Artisan Bread</div>
                    </div>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <Modal
        title={null}
        open={isModalVisible}
        onCancel={handleCancel}
        footer={null}
        width={800}
        destroyOnClose={true}
        bodyStyle={{ padding: 0 }}
      >
        <div
          style={{
            backgroundColor: token.colorBgContainer,
            borderRadius: "12px",
          }}
        >
          {/* Custom Header */}
          <div style={headerStyle}>
            <ChefHat size={28} color={token.colorPrimary} />
            <Title level={3} style={{ margin: 0, color: token.colorText }}>
              Share Your Culinary Creation
            </Title>
          </div>

          <Form
            form={form}
            layout="vertical"
            onFinish={onFinish}
            autoComplete="off"
            style={{ padding: "24px 30px" }}
          >
            {/* Basic Info Section */}
            <Card
              title={
                <Text strong>
                  <BookOpen size={16} style={{ marginBottom: "-3px" }} /> Recipe
                  Basics
                </Text>
              }
              style={formItemStyle}
              bodyStyle={{ padding: "16px" }}
            >
              <Form.Item
                name="title"
                label="Recipe Title"
                rules={[
                  { required: true, message: "Please enter the recipe name!" },
                ]}
              >
                <Input placeholder="e.g., Spicy Garlic & Honey Glazed Salmon" />
              </Form.Item>

              <Form.Item
                name="subtitle"
                label="Short Summary / Subtitle"
                rules={[
                  { required: true, message: "A quick summary is needed!" },
                ]}
              >
                <Input placeholder="A stunning weeknight dish with minimal effort..." />
              </Form.Item>

              <Form.Item
                name="description"
                label="Recipe Story / Detailed Description"
                rules={[
                  {
                    required: true,
                    message: "Please tell us about your dish!",
                  },
                ]}
              >
                <TextArea
                  rows={4}
                  placeholder="What inspired this recipe? What's the best way to serve it?"
                />
              </Form.Item>
            </Card>

            {/* Ingredients Section */}
            <Card
              title={
                <Text strong>
                  <ChefHat size={16} style={{ marginBottom: "-3px" }} />{" "}
                  Ingredients
                </Text>
              }
              style={formItemStyle}
              bodyStyle={{ padding: "16px" }}
            >
              <Form.List name="ingredients" initialValue={["", ""]}>
                {(fields, { add, remove }) => (
                  <>
                    {fields.map(({ key, name, fieldKey, ...restField }) => (
                      <Space
                        key={key}
                        style={{ display: "flex", marginBottom: 8 }}
                        align="baseline"
                      >
                        <Form.Item
                          {...restField}
                          name={[name]}
                          fieldKey={[fieldKey, "ingredient"]}
                          rules={[
                            { required: true, message: "Missing ingredient" },
                          ]}
                          style={{ margin: 0, flexGrow: 1 }}
                        >
                          <Input
                            placeholder="e.g., 2 Salmon Fillets (6oz each)"
                            style={{ width: 350 }}
                          />
                        </Form.Item>
                        <MinusCircle
                          onClick={() => remove(name)}
                          style={{ color: token.colorError }}
                        />
                      </Space>
                    ))}
                    <Button
                      type="dashed"
                      onClick={() => add()}
                      block
                      icon={<Plus />}
                    >
                      Add Ingredient
                    </Button>
                  </>
                )}
              </Form.List>
            </Card>

            {/* Steps/Instructions Section */}
            <Card
              title={
                <Text strong>
                  <ListOrdered size={16} style={{ marginBottom: "-3px" }} />{" "}
                  Instructions (Steps)
                </Text>
              }
              style={formItemStyle}
              bodyStyle={{ padding: "16px" }}
            >
              <Form.List name="steps" initialValue={["", ""]}>
                {(fields, { add, remove }) => (
                  <>
                    {fields.map(
                      ({ key, name, fieldKey, ...restField }, index) => (
                        <div key={key} style={{ marginBottom: 12 }}>
                          <Space align="start" style={{ width: "100%" }}>
                            <Text strong style={{ minWidth: "20px" }}>
                              {index + 1}.
                            </Text>
                            <Form.Item
                              {...restField}
                              name={[name]}
                              fieldKey={[fieldKey, "step"]}
                              rules={[
                                {
                                  required: true,
                                  message: "Missing instruction step",
                                },
                              ]}
                              style={{ margin: 0, flexGrow: 1 }}
                            >
                              <TextArea
                                rows={2}
                                placeholder="Detail the step clearly (e.g., Preheat oven to 400°F...)"
                              />
                            </Form.Item>
                            <MinusCircle
                              onClick={() => remove(name)}
                              style={{
                                color: token.colorError,
                                marginTop: "8px",
                              }}
                            />
                          </Space>
                          <Divider style={{ margin: "8px 0 0 0" }} />
                        </div>
                      )
                    )}
                    <Button
                      type="dashed"
                      onClick={() => add()}
                      block
                      icon={<Plus />}
                    >
                      Add Instruction Step
                    </Button>
                  </>
                )}
              </Form.List>
            </Card>

            {/* Metadata Section */}
            <Card
              title={
                <Text strong>
                  <BookOpen size={16} style={{ marginBottom: "-3px" }} />{" "}
                  Optional Details
                </Text>
              }
              style={formItemStyle}
              bodyStyle={{ padding: "16px" }}
            >
              <Form.Item
                name="tags"
                label="Tags"
                tooltip="Enter tags separated by commas (e.g., Dinner, Spicy, Healthy)"
              >
                <Input placeholder="e.g., Dinner, Fish, Quick, Weeknight" />
              </Form.Item>

              <Form.Item name="imageUrl" label="Feature Image URL">
                <Input placeholder="Optional: Paste a link to your recipe image" />
              </Form.Item>

              <Form.Item name="authorName" label="Your Chef Name">
                <Input placeholder="e.g., Chef Alex" />
              </Form.Item>
            </Card>

            <Form.Item style={{ marginTop: "30px" }}>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                icon={<Send size={18} />}
                block
                size="large"
              >
                {loading ? "Posting..." : "Publish Recipe"}
              </Button>
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </>
  );
};

export default Home;
