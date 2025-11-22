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
  Divider,
  Avatar,
  Tag,
  Select,
  Upload,
  Pagination,
  AutoComplete,
} from "antd";
import { HeartOutlined, MessageFilled, HeartFilled } from "@ant-design/icons";
import {
  Plus,
  MinusCircle,
  Send,
  ChefHat,
  ListOrdered,
  BookOpen,
  Bookmark,
  UploadIcon,
} from "lucide-react";
import { useEffect, useState } from "react";

const { TextArea } = Input;
const { Title, Text } = Typography;

const Home = () => {
  const { token } = theme.useToken();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [allRecipes, setAllRecipes] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [myCookbook, setMyCookbook] = useState(false);
  const [createRecipeForm, setCreateRecipeForm] = useState({
    title: "",
    description: "",
    ingredients: [],
    preparation: [],
    cuisine: "",
    recipeImageUrl: "",
  });
  console.log("Create Recipe Form State:", createRecipeForm);
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
    fetchAllRecipes();
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
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
      }
      if (!response.ok) {
        throw new Error("Failed to fetch user profile");
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

  const fetchAllRecipes = async (userId, pageNumber) => {
    if (pageNumber === undefined || pageNumber === null) {
      pageNumber = 1;
    }
    try {
      const url = userId
        ? `http://localhost:8089/api/v1/recipes?userId=${userId}&page=${pageNumber}&size=10&sortBy=id&direction=asc`
        : `http://localhost:8089/api/v1/recipes?page=${pageNumber}&size=10&sortBy=id&direction=asc`;
      const response = await fetch(url, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
      }
      if (!response.ok) {
        throw new Error("Failed to fetch recipes");
      }
      const data = await response.json();
      console.log("Fetched Recipes:", data.content);
      setAllRecipes(data.content || []);
      setPage(data.page);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error("Error fetching recipes:", error);
    }
  };

  const likeAndUnlikeRecipe = async (recipeId) => {
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
      setAllRecipes((prevRecipes) =>
        prevRecipes.map((recipe) =>
          recipe.id === recipeId
            ? {
                ...recipe,
                likedByUser: !recipe.likedByUser,
                likesCount: recipe.likedByUser
                  ? recipe.likesCount - 1
                  : recipe.likesCount + 1,
              }
            : recipe
        )
      );
    } catch (error) {
      console.error("Error liking/unliking recipe:", error);
    }
  };

  const handleCancel = () => {
    setIsModalVisible(false);
    form.resetFields();
  };

  const handleSaveUnsave = async (recipeId) => {
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
      setAllRecipes((prevRecipes) =>
        prevRecipes.map((recipe) =>
          recipe.id === recipeId
            ? { ...recipe, savedByUser: !recipe.savedByUser }
            : recipe
        )
      );
    } catch (error) {
      console.error("Error saving/unsaving recipe:", error);
    }
  };

  const normFile = (e) => {
    if (Array.isArray(e)) {
      return e;
    }
    return e?.fileList;
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

  const onFinish = async () => {
    const avatarFile = form.getFieldValue("avatar")?.[0]?.originFileObj;
    const uploadPromise = avatarFile
      ? uploadPhoto(avatarFile)
      : Promise.resolve();

    try {
      setLoading(true);
      await uploadPromise;

      const formattedPreparation = (createRecipeForm.preparation || []).map(
        (step) => ({
          step: step,
        })
      );
      const payload = {
        ...createRecipeForm,
        preparation: formattedPreparation,
        recipeImageUrl: avatarFile ? await uploadPromise : "",
      };
      console.log("Payload to be sent:", payload);

      const response = await fetch("http://localhost:8089/api/v1/recipes", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(payload),
      });
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
      }
      if (!response.ok) {
        throw new Error("Failed to create recipe");
      }
      console.log("Recipe created successfully");
      handleCancel();
      fetchAllRecipes();
    } catch (error) {
      console.error("Error creating recipe:", error);
    } finally {
      setLoading(false);
    }
  };

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
                  onClick={() => {
                    fetchAllRecipes(userProfile.id);
                    setMyCookbook(true);
                  }}
                >
                  My Cookbook
                </a>
                <a
                  href="#"
                  className="profile-link"
                  onClick={() => {
                    fetchAllRecipes();
                    setMyCookbook(false);
                  }}
                >
                  All Recipes
                </a>
              </div>
            </div>
          </div>
          <div className="feed-column">
            <div className="feed-controls">
              <div className="search-container">
                <AutoComplete
                  // className="search-bar"
                  style={{
                    minWidth: 625,
                    borderRadius: "8px",
                    minHeight: "40px",
                  }}
                  showSearch={{
                    filter: (inputValue, option) =>
                      option.value
                        .toUpperCase()
                        .includes(inputValue.toUpperCase()),
                  }}
                  options={[
                    { value: "Spaghetti Carbonara" },
                    { value: "Vegan Tacos" },
                    { value: "Chocolate Lava Cake" },
                    { value: "Sushi Rolls" },
                    { value: "Mediterranean Salad" },
                  ]}
                  // Replace onfocus with React event
                  onFocus={() => console.log("Mock search activated.")}
                ></AutoComplete>
              </div>
              <button
                className="feed-post-btn"
                onClick={() => setIsModalVisible(true)}
              >
                <Upload /> Share Recipe
              </button>
            </div>

            <h2>Latest Community Recipes</h2>

            {allRecipes.length === 0 ? (
              <p>No recipes found. Be the first to share a recipe!</p>
            ) : (
              allRecipes.map((recipe) => (
                <div className="recipe-card" key={recipe.id}>
                  <img
                    src={
                      recipe?.recipeImageUrl ||
                      "https://placehold.co/800x600/ff7f50/ffffff?text=Vibrant+Salmon+Recipe"
                    }
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src =
                        "https://placehold.co/800x600/ccc/333?text=Image+Load+Failed";
                    }}
                    alt={recipe.title || "Recipe Image"}
                    className="recipe-image"
                  />
                  <div className="card-content">
                    <div className="card-author-info">
                      <div className="author-avatar">
                        {recipe?.username?.charAt(0).toUpperCase() || "U"}
                      </div>
                      <div>
                        <h3>{recipe?.title}</h3>
                        <div className="author-name">
                          Posted by{" "}
                          <a
                            href="#"
                            onClick={() =>
                              navigate(`/profile/${recipe.userId}`, {
                                state: { loggedInUserId: userProfile.id },
                              })
                            }
                          >
                            @{recipe?.username}
                          </a>
                        </div>
                      </div>
                    </div>
                    <p className="recipe-description">
                      {recipe?.description}
                      <br />
                      <a
                        href="#"
                        onClick={() => navigate(`/recipe/${recipe.id}`)}
                      >
                        {" "}
                        Read more
                      </a>
                    </p>
                    <div>
                      <Tag color="#f50">{recipe?.cuisine}</Tag>
                    </div>
                    <div className="card-divider"></div>
                    <div className="card-actions">
                      <button
                        className="action-button like-button"
                        onClick={() => likeAndUnlikeRecipe(recipe.id)}
                      >
                        {recipe?.likedByUser ? (
                          <HeartFilled style={{ color: "red" }} />
                        ) : (
                          <HeartOutlined />
                        )}
                        <span style={{ marginLeft: "5px", fontWeight: "500" }}>
                          {recipe?.likesCount}
                        </span>
                      </button>
                      <button
                        className="action-button"
                        onClick={() => navigate(`/recipe/${recipe.id}`)}
                      >
                        <MessageFilled />
                        <span style={{ marginLeft: "5px", fontWeight: "500" }}>
                          {recipe?.commentCount}
                        </span>
                      </button>
                      <button
                        className="action-button"
                        onClick={() => handleSaveUnsave(recipe.id)}
                      >
                        {recipe?.savedByUser ? (
                          <Bookmark
                            style={{
                              color: "#1890ff",
                              fill: "#1890ff",
                            }}
                          />
                        ) : (
                          <Bookmark />
                        )}
                        <span style={{ marginLeft: "5px", fontWeight: "500" }}>
                          {recipe?.savedByUser ? "Saved" : "Save"}
                        </span>
                      </button>
                    </div>
                  </div>
                </div>
              ))
            )}
            <Pagination
              current={page}
              total={totalPages * 10}
              onChange={(pageNumber) => {
                fetchAllRecipes(myCookbook ? userProfile.id : null, pageNumber);
              }}
              style={{
                display: "flex",
                justifyContent: "center",
                marginTop: "20px",
              }}
            />
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
            onValuesChange={(changedValues, allValues) => {
              setCreateRecipeForm({
                ...createRecipeForm,
                ...allValues,
              });
            }}
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
              <Form.List
                name="ingredients"
                initialValue={[
                  { name: "", quantity: "" },
                  { name: "", quantity: "" },
                ]}
              >
                {(fields, { add, remove }) => (
                  <>
                    {fields.map(({ key, name, ...restField }) => (
                      <Space
                        key={key}
                        style={{ display: "flex", marginBottom: 8 }}
                        align="baseline"
                      >
                        <Form.Item
                          {...restField}
                          name={[name, "name"]}
                          rules={[
                            {
                              required: true,
                              message: "Missing ingredient name",
                            },
                          ]}
                          style={{ margin: 0, flexGrow: 1 }}
                        >
                          <Input
                            placeholder="Ingredient name (e.g., Paneer)"
                            style={{ width: 180 }}
                          />
                        </Form.Item>
                        <Form.Item
                          {...restField}
                          name={[name, "quantity"]}
                          rules={[
                            { required: true, message: "Missing quantity" },
                          ]}
                          style={{ margin: 0, flexGrow: 1 }}
                        >
                          <Input
                            placeholder="Quantity (e.g., 250 grams (cubed))"
                            style={{ width: 180 }}
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
              <div style={{ marginTop: 12 }}>
                <Text type="secondary" style={{ fontSize: 13 }}>
                  Add each ingredient one by one. In the description, use "-"
                  before each ingredient (e.g., "- 250g Paneer").
                </Text>
              </div>
            </Card>
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
              <Form.List name="preparation" initialValue={["", ""]}>
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
              <div style={{ marginTop: 12 }}>
                <Text type="secondary" style={{ fontSize: 13 }}>
                  Add each step one by one. In the description, use "-" before
                  each step (e.g., "- Preheat oven to 400°F").
                </Text>
              </div>
            </Card>

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
              <Form.Item name="cuisine" label="Cuisine">
                <Select placeholder="Select cuisine type" allowClear>
                  <Select.Option value="Italian">Italian</Select.Option>
                  <Select.Option value="Chinese">Chinese</Select.Option>
                  <Select.Option value="Indian">Indian</Select.Option>
                  <Select.Option value="Mexican">Mexican</Select.Option>
                  <Select.Option value="French">French</Select.Option>
                  <Select.Option value="Japanese">Japanese</Select.Option>
                  <Select.Option value="Mediterranean">
                    Mediterranean
                  </Select.Option>
                  <Select.Option value="Thai">Thai</Select.Option>
                  <Select.Option value="Spanish">Spanish</Select.Option>
                  <Select.Option value="American">American</Select.Option>
                </Select>
              </Form.Item>

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
