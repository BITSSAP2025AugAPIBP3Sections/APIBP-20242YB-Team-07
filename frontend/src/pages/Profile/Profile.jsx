import React, { useState, useEffect } from "react";
import {
  Layout,
  Typography,
  Button,
  Card,
  theme,
  Space,
  Divider,
  Avatar,
  Tabs,
  Modal,
  Form,
  Input,
  Upload,
  List,
  Tag,
  Select,
  notification,
} from "antd";
import {
  Edit,
  Camera,
  BookOpen,
  Bookmark,
  Trophy,
  User,
  Mail,
  Upload as UploadIcon,
} from "lucide-react";
import { useParams } from "react-router-dom";
import LoggedInNavbar from "../../components/Navbar/LoggedInNavbar/LoggedInNavbar";
import { useAuth } from "../../auth/AuthContext";

const { Content, Footer } = Layout;
const { Title, Paragraph, Text } = Typography;
const { TabPane } = Tabs;

const mockPostedRecipes = [
  {
    id: 1,
    title: "Classic Italian Tiramisu",
    image: "https://placehold.co/400x300/ff7f50/ffffff?text=Tiramisu",
  },
  {
    id: 2,
    title: "Spicy Sriracha Salmon",
    image: "https://placehold.co/400x300/ff6f61/ffffff?text=Salmon",
  },
  {
    id: 3,
    title: "Vegan Lentil Bolognese",
    image: "https://placehold.co/400x300/87d068/ffffff?text=Lentils",
  },
  {
    id: 4,
    title: "Homemade Sourdough",
    image: "https://placehold.co/400x300/faad14/ffffff?text=Bread",
  },
];

const mockSavedRecipes = [
  {
    id: 5,
    title: "Thai Green Curry",
    image: "https://placehold.co/400x300/52c41a/ffffff?text=Curry",
  },
  {
    id: 6,
    title: "Crispy Smashed Potatoes",
    image: "https://placehold.co/400x300/fadb14/ffffff?text=Potatoes",
  },
];

const mockChallenges = [
  {
    id: 1,
    title: "Holiday Cookie Challenge",
    status: "Completed",
    award: "Top 10 Finalist",
  },
  { id: 2, title: "30-Minute Meals", status: "In Progress" },
  {
    id: 3,
    title: "Summer Grilling Contest",
    status: "Completed",
    award: "Runner Up",
  },
];

// --- CSS Styles (Plain CSS-in-JS) ---
const styles = {
  layout: {
    minHeight: "100vh",
    fontFamily: "Inter, sans-serif",
  },
  header: {
    padding: "0 24px",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    zIndex: 10,
    boxShadow: "0 2px 4px rgba(0, 0, 0, 0.05)",
    height: "64px",
  },
  logo: {
    display: "flex",
    alignItems: "center",
    fontSize: "24px",
    fontWeight: "800",
    color: "#1890ff",
  },
  contentArea: {
    padding: "48px 24px",
    maxWidth: "1200px",
    margin: "0 auto",
  },
  // --- Profile Header Card ---
  profileHeaderCard: {
    padding: "32px",
    borderRadius: "16px",
    boxShadow: "0 10px 30px rgba(0, 0, 0, 0.08)",
    display: "flex",
    alignItems: "center",
    gap: "32px",
    borderTop: "8px solid #FF6F61", // Theme accent
  },
  // --- Creative Avatar Container ---
  avatarContainer: {
    position: "relative",
    cursor: "pointer",
    width: "150px",
    height: "150px",
  },
  avatarOverlay: {
    position: "absolute",
    top: 0,
    left: 0,
    width: "100%",
    height: "100%",
    borderRadius: "50%",
    backgroundColor: "rgba(0, 0, 0, 0.5)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    color: "#fff",
    fontSize: "24px",
    opacity: 0,
    transition: "opacity 0.3s ease-in-out",
  },
  // --- Recipe Grid ---
  recipeList: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fill, minmax(250px, 1fr))", // Responsive grid
    gap: "24px",
    marginTop: "24px",
  },
  recipeCard: {
    borderRadius: "12px",
    overflow: "hidden",
    boxShadow: "0 4px 12px rgba(0, 0, 0, 0.05)",
  },
  recipeCardImage: {
    height: "200px",
    width: "100%",
    objectFit: "cover",
  },
  // --- Footer ---
  footer: {
    textAlign: "center",
    padding: "24px 0",
    backgroundColor: "#f0f2f5",
    color: "#888",
    borderTop: "1px solid #e8e8e8",
  },
};

// --- Helper Components ---

/**
 * Creative Avatar with Hover Effect
 */
const ProfileAvatar = ({ src, onEditClick }) => {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <div
      style={styles.avatarContainer}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={onEditClick}
    >
      <Avatar size={150} src={src} />
      <div
        style={{
          ...styles.avatarOverlay,
          opacity: isHovered ? 1 : 0, // Control opacity based on state
        }}
      >
        <Camera size={40} />
      </div>
    </div>
  );
};

/**
 * Reusable Recipe Card
 */
const RecipeCard = ({ recipe }) => (
  <Card
    hoverable
    style={styles.recipeCard}
    cover={
      <img
        alt={recipe.title}
        src={recipe.recipeImageUrl}
        style={styles.recipeCardImage}
      />
    }
  >
    <Card.Meta title={recipe.title} />
  </Card>
);

/**
 * Main Profile Page Component
 */
const Profile = () => {
  const { id: userId } = useParams();
  const { user } = useAuth();

  const { token } = theme.useToken();
  const [form] = Form.useForm();
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

  // --- State ---
  const [fetchedUserProfile, setFetchedUserProfile] = useState({
    email: "",
    role: "",
    username: "",
    fullName: "",
    dietaryPreference: "",
    healthGoal: "",
    avatarUrl: "",
    bio: "",
    cuisinePreferences: [],
  });
  useEffect(() => {
    fetchUserProfile();
    fetchPostedRecipes();
    fetchSavedRecipes();
  }, []);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [postedRecipes, setPostedRecipes] = useState([]);
  const [savedRecipes, setSavedRecipes] = useState([]);

  const fetchUserProfile = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        userId
          ? `http://localhost:8089/api/v1/users/${userId}`
          : `http://localhost:8089/api/v1/users/user-details`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      const data = await response.json();
      if (response.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
      }
      setFetchedUserProfile(data);
    } catch (error) {
      console.error("Error fetching user profile:", error);
    }
  };
  console.log(user);

  const fetchPostedRecipes = async () => {
    try {
      const response = await fetch(
        `http://localhost:8089/api/v1/recipes?userId=${userId}`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      const data = await response.json();
      console.log("sachit is", data);
      if (response.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
      }
      if (data.length === 0) {
        setPostedRecipes([]);
      } else {
        setPostedRecipes(data);
      }
    } catch (error) {
      console.error("Error fetching posted recipes:", error);
    }
  };

  const fetchSavedRecipes = async () => {
    try {
      const response = await fetch(
        `http://localhost:8089/api/v1/recipes?userId=${userId}&saved=true`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      const data = await response.json();
      console.log("saved recipes", data);
      if (response.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
      }
      if (data.length === 0) {
        setSavedRecipes([]);
      } else {
        setSavedRecipes(data);
      }
    } catch (error) {
      console.error("Error fetching saved recipes:", error);
    }
  };

  // --- Handlers ---
  const showEditModal = () => {
    form.setFieldsValue({
      username: fetchedUserProfile.username,
      email: fetchedUserProfile.email,
      bio: fetchedUserProfile.bio,
      fullName: fetchedUserProfile.fullName,
      dietaryPreference: fetchedUserProfile.dietaryPreference,
      healthGoal: fetchedUserProfile.healthGoal,
      cuisinePreferences: fetchedUserProfile.cuisinePreferences,
    });
    setIsModalVisible(true);
  };

  const handleModalCancel = () => {
    setIsModalVisible(false);
  };

  const uploadPhoto = async (file) => {
    const formData = new FormData();
    formData.append("image", file);

    const response = await fetch(
      `https://api.imgbb.com/1/upload?key=80994bfe4a9255cfc684dadf5402c438`,
      {
        method: "POST",
        body: formData,
      }
    );

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.error.message || "Image upload failed");
    }
    console.log(data);
    return data.data.url;
  };

  const handleModalOk = () => {
    const avatarFile = form.getFieldValue("avatar")?.[0]?.originFileObj;
    const uploadPromise = avatarFile
      ? uploadPhoto(avatarFile).then((url) => {
          form.setFieldsValue({ avatar: [{ thumbUrl: url }] });
        })
      : Promise.resolve();

    uploadPromise
      .then(() => form.validateFields())
      .then(async (values) => {
        try {
          const token = localStorage.getItem("token");
          const response = await fetch("http://localhost:8089/api/v1/users/4", {
            method: "PUT",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
              username: values.username,
              email: values.email,
              bio: values.bio,
              avatarUrl:
                values.avatar?.[0]?.thumbUrl || fetchedUserProfile.avatarUrl,
              dietaryPreference: values.dietaryPreference,
              healthGoal: values.healthGoal,
              cuisinePreferences: values.cuisinePreferences,
              fullName: values.fullName,
            }),
          });

          const data = await response.json();

          if (!response.ok) {
            throw new Error(data.message || "Failed to update profile");
          }

          setFetchedUserProfile({
            ...fetchedUserProfile,
            username: values.username,
            email: values.email,
            bio: values.bio,
            avatarUrl:
              values.avatar?.[0]?.thumbUrl || fetchedUserProfile.avatarUrl,
            dietaryPreference: values.dietaryPreference,
            healthGoal: values.healthGoal,
            cuisinePreferences: values.cuisinePreferences,
            fullName: values.fullName,
          });

          form.resetFields();
          setIsModalVisible(false);

          openNotification(
            false,
            "success",
            "Profile updated successfully!",
            "Your profile has been updated successfully."
          )();
        } catch (err) {
          openNotification(
            false,
            "error",
            "Profile update failed",
            err.message
          )();
        }
      })
      .catch((info) => {
        console.log("Validate Failed:", info);
      });
  };

  // Helper for Upload component
  const normFile = (e) => {
    if (Array.isArray(e)) {
      return e;
    }
    return e?.fileList;
  };

  return (
    <Layout style={styles.layout}>
      {contextHolder}
      {/* 1. Header (Static Nav) */}
      <LoggedInNavbar activeKey="4" />

      {/* 2. Main Content Area */}
      <Content>
        <div style={styles.contentArea}>
          <Card style={styles.profileHeaderCard}>
            <ProfileAvatar
              src={
                fetchedUserProfile.avatarUrl ||
                "https://img.freepik.com/premium-vector/cartoon-chef-with-thumbs-up-sign-that-says-thumbs-up_1166763-15878.jpg"
              }
              onEditClick={
                user.userData.id === fetchedUserProfile.id
                  ? showEditModal
                  : undefined
              }
            />
            <div style={{ flex: 1 }}>
              <Title level={2} style={{ margin: 0, fontWeight: "800" }}>
                {fetchedUserProfile.username}
              </Title>
              <Paragraph
                style={{ color: token.colorTextSecondary, fontSize: "16px" }}
              >
                {fetchedUserProfile.fullName}
              </Paragraph>
              <Paragraph
                style={{ color: token.colorTextSecondary, fontSize: "16px" }}
              >
                {fetchedUserProfile.email}
              </Paragraph>
              <Paragraph style={{ marginTop: "16px" }}>
                {fetchedUserProfile.bio}
              </Paragraph>
              <Paragraph style={{ marginTop: "16px" }}>
                <Text strong style={{ fontSize: "16px" }}>
                  Dietary Preference:{" "}
                </Text>{" "}
                <Tag color="red" style={{ fontSize: "16px" }}>
                  {fetchedUserProfile.dietaryPreference || "Not specified"}
                </Tag>
                <br />
              </Paragraph>
              <Paragraph style={{ marginTop: "16px" }}>
                <Text strong style={{ fontSize: "16px" }}>
                  Health Goal:{" "}
                </Text>{" "}
                <Tag color="volcano" style={{ fontSize: "16px" }}>
                  {fetchedUserProfile.healthGoal || "Not specified"}
                </Tag>
              </Paragraph>
              <Paragraph style={{ marginTop: "16px" }}>
                <Text strong style={{ fontSize: "16px" }}>
                  Cuisine Preferences:{" "}
                </Text>{" "}
                {fetchedUserProfile.cuisinePreferences &&
                fetchedUserProfile.cuisinePreferences.length > 0 ? (
                  fetchedUserProfile.cuisinePreferences.map(
                    (cuisine, index) => (
                      <Tag
                        color="blue"
                        key={index}
                        style={{ fontSize: "16px" }}
                      >
                        {cuisine}
                      </Tag>
                    )
                  )
                ) : (
                  <Tag color="blue" style={{ fontSize: "16px" }}>
                    Not specified
                  </Tag>
                )}
              </Paragraph>
              {fetchedUserProfile.id === user.userData.id && (
                <Button
                  type="primary"
                  icon={<Edit size={16} />}
                  style={{ marginTop: "16px", fontWeight: "600" }}
                  onClick={showEditModal}
                >
                  Edit Profile
                </Button>
              )}
            </div>
          </Card>

          <Divider style={{ margin: "40px 0" }} />

          {/* --- Content Tabs --- */}
          <Card style={{ borderRadius: "16px" }}>
            <Tabs
              defaultActiveKey="1"
              size="large"
              style={{ padding: "0 16px" }}
            >
              {/* Tab 1: Posted Recipes */}
              <TabPane
                tab={
                  <Space>
                    <BookOpen /> Posted Recipes ({postedRecipes.length})
                  </Space>
                }
                key="1"
              >
                <div style={styles.recipeList}>
                  {postedRecipes.map((recipe) => (
                    <RecipeCard key={recipe.id} recipe={recipe} />
                  ))}
                </div>
              </TabPane>

              {/* Tab 2: Saved Recipes */}
              <TabPane
                tab={
                  <Space>
                    <Bookmark /> Saved Recipes ({savedRecipes.length})
                  </Space>
                }
                key="2"
              >
                <div style={styles.recipeList}>
                  {savedRecipes.map((recipe) => (
                    <RecipeCard key={recipe.id} recipe={recipe} />
                  ))}
                </div>
              </TabPane>

              {/* Tab 3: My Challenges */}
              <TabPane
                tab={
                  <Space>
                    <Trophy /> My Challenges ({mockChallenges.length})
                  </Space>
                }
                key="3"
              >
                <List
                  itemLayout="horizontal"
                  dataSource={mockChallenges}
                  renderItem={(item) => (
                    <List.Item
                      actions={[
                        <Tag
                          color={item.status === "Completed" ? "green" : "blue"}
                        >
                          {item.status}
                        </Tag>,
                      ]}
                    >
                      <List.Item.Meta
                        title={<Text strong>{item.title}</Text>}
                        description={
                          item.award
                            ? `Result: ${item.award}`
                            : "No results yet."
                        }
                      />
                    </List.Item>
                  )}
                />
              </TabPane>
            </Tabs>
          </Card>
        </div>
      </Content>

      {/* 3. Footer */}
      <Footer style={styles.footer}>
        Cookbook ©{new Date().getFullYear()} | Manage your culinary world.
      </Footer>

      {/* --- Edit Profile Modal --- */}
      <Modal
        title="Edit Your Profile"
        open={isModalVisible}
        onOk={handleModalOk}
        onCancel={handleModalCancel}
        okText="Save Changes"
      >
        <Form form={form} layout="vertical" name="edit_profile_form">
          <Form.Item
            name="avatar"
            label="Profile Picture"
            valuePropName="fileList"
            getValueFromEvent={normFile}
          >
            <Upload
              name="avatar"
              listType="picture-card"
              maxCount={1}
              beforeUpload={() => false} // Prevent automatic upload
            >
              <div>
                <UploadIcon />
                <div style={{ marginTop: 8 }}>Upload</div>
              </div>
            </Upload>
          </Form.Item>

          <Form.Item
            name="username"
            label="Username"
            rules={[{ required: true, message: "Please input your username!" }]}
          >
            <Input prefix={<User size={16} />} />
          </Form.Item>

          <Form.Item
            name="email"
            label="Email"
            rules={[
              {
                required: true,
                message: "Please input your email!",
                type: "email",
              },
            ]}
          >
            <Input prefix={<Mail size={16} />} />
          </Form.Item>

          <Form.Item
            name="fullName"
            label="Full Name"
            rules={[
              { required: true, message: "Please input your full name!" },
            ]}
          >
            <Input prefix={<User size={16} />} />
          </Form.Item>

          <Form.Item name="bio" label="Your Bio">
            <Input.TextArea
              rows={4}
              placeholder="Tell the community about yourself"
            />
          </Form.Item>

          <Form.Item name="dietaryPreference" label="Dietary Preference">
            <Select
              placeholder="Select dietary preference"
              allowClear
              options={[
                { value: "Vegan", label: "Vegan" },
                { value: "Vegetarian", label: "Vegetarian" },
                { value: "Keto", label: "Keto" },
                { value: "Paleo", label: "Paleo" },
                { value: "Gluten-Free", label: "Gluten-Free" },
                { value: "None", label: "None" },
              ]}
            />
          </Form.Item>

          <Form.Item name="healthGoal" label="Health Goal">
            <Select
              placeholder="Select health goal"
              allowClear
              options={[
                { value: "Weight Loss", label: "Weight Loss" },
                { value: "Muscle Gain", label: "Muscle Gain" },
                { value: "General Wellness", label: "General Wellness" },
                { value: "Endurance", label: "Endurance" },
                { value: "None", label: "None" },
              ]}
            />
          </Form.Item>

          <Form.Item name="cuisinePreferences" label="Cuisine Preferences">
            <Select
              mode="multiple"
              placeholder="Select cuisine preferences"
              allowClear
              options={[
                { value: "Italian", label: "Italian" },
                { value: "Mexican", label: "Mexican" },
                { value: "Indian", label: "Indian" },
                { value: "Chinese", label: "Chinese" },
                { value: "Thai", label: "Thai" },
                { value: "French", label: "French" },
                { value: "Japanese", label: "Japanese" },
                { value: "Mediterranean", label: "Mediterranean" },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

export default Profile;
