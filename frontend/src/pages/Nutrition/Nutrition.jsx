import React, { useState, useEffect, useMemo } from 'react';
import { Card, Progress, Row, Col, Statistic, List, Typography, Space, Button, Modal, Form, Input, InputNumber, message, Table } from 'antd';
import { Dumbbell, Calendar, ArrowUpRight, ArrowDownRight, Sun, Apple, Moon, Leaf, Plus } from 'lucide-react';

// Placeholder imports for useAuth and LoggedInNavbar have been removed 
import useAuth from '../../auth/AuthContext';
import LoggedInNavbar from '../../components/Navbar/LoggedInNavbar/LoggedInNavbar';

const { Title, Text } = Typography;

// --- Configuration ---
const API_BASE_URL = 'http://localhost:8089/api/v1/nutrition/';
const MEAL_TYPES_TO_FETCH = ['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER'];

// --- Colors ---
const PRIMARY_COLOR = '#1890ff';
const PRIMARY_BG_COLOR = '#e6f7ff';
const fibreColor = '#52c41a';
const carbsColor = PRIMARY_COLOR;
const fatColor = '#f5222d';
const sugarColor = '#faad14';

// Default structure and colors for meals
const MEAL_STRUCTURE_DEFAULTS = {
  Breakfast: {
    bgColor: '#fffbe6',
    icon: <Sun style={{ width: '20px', height: '20px', color: '#faad14' }} />,
    items: [],
  },
  Lunch: {
    bgColor: '#f6ffed',
    icon: <Apple style={{ width: '20px', height: '20px', color: '#52c41a' }} />,
    items: [],
  },
  Snacks: {
    bgColor: '#e6fffb',
    icon: <Leaf style={{ width: '20px', height: '20px', color: '#13c2c2' }} />,
    items: [],
  },
  Dinner: {
    bgColor: '#f0f5ff',
    icon: <Moon style={{ width: '20px', height: '20px', color: PRIMARY_COLOR }} />,
    items: [],
  },
};

// Utility to create a deep copy of the default meal structure (required because of React elements/icons)
const getInitialMealData = () => {
    return Object.fromEntries(
        Object.entries(MEAL_STRUCTURE_DEFAULTS).map(([key, value]) => [
            key,
            { ...value, items: [] } 
        ])
    );
};

// --- Sub-Components ---

const MacroGoalProgress = ({ label, current, goal, unit, color, loading }) => {
  const percent = Math.min(100, Math.round((current / goal) * 100));
  const diff = current - goal;
  const statusColor = diff > 0 ? '#ff4d4f' : '#52c41a';

  return (
    <Card 
      bordered={false}
      loading={loading}
      style={{ 
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)',
        borderLeft: `4px solid ${color}`,
        borderRadius: '6px'
      }}
    >
      <Space direction="vertical" size="small" style={{ width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <Statistic
            title={label}
            value={current.toFixed(1)}
            suffix={unit}
            valueStyle={{ color: color, fontWeight: 'bold' }}
          />
          <Progress type="circle" percent={percent} width={60} strokeColor={color} format={() => `${percent}%`} />
        </div>
        <Text type="secondary" style={{ fontSize: '12px' }}>Goal: {goal} {unit}</Text>
        <Text style={{ color: statusColor, display: 'flex', alignItems: 'center', fontSize: '12px' }}>
          {diff > 0 ? <ArrowUpRight size={12} style={{marginRight: 4}}/> : <ArrowDownRight size={12} style={{marginRight: 4}}/>}
          {Math.abs(diff).toFixed(1)} {unit} {diff > 0 ? 'Over' : 'Remaining'}
        </Text>
      </Space>
    </Card>
  );
};

const MealCard = ({ title, data, icon, bgColor, onAddClick, loading }) => {
  // calculate local totals for this specific meal
  let localFibre = 0, localCarbs = 0, localFat = 0, localSugar = 0;
  
  if (!loading) {
    data.items.forEach(item => {
      localFibre += item.fibre || 0;
      localCarbs += item.carbs || 0;
      localFat += item.fat || 0;
      localSugar += item.sugar || 0;
    });
  }

  return (
    <Card 
      title={
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            {icon}
            <Title level={4} style={{ margin: 0 }}>{title}</Title>
          </div>
          <Button 
            type="text" 
            icon={<Plus size={18} />} 
            onClick={() => onAddClick(title)}
            style={{ color: '#595959' }}
          />
        </div>
      }
      loading={loading}
      style={{ 
        height: '100%', 
        backgroundColor: bgColor,
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)',
        borderRadius: '8px'
      }}
      bodyStyle={{ padding: '16px' }}
    >
      <div style={{ marginBottom: '16px' }}>
        <Text strong style={{ fontSize: '16px', display: 'block', marginBottom: '8px' }}>Macro Breakdown (g)</Text>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', fontSize: '14px' }}>
          <Text style={{ color: fibreColor }}>Fibre: {localFibre.toFixed(1)}g</Text>
          <Text style={{ color: carbsColor }}>Carbs: {localCarbs.toFixed(1)}g</Text>
          <Text style={{ color: fatColor }}>Fat: {localFat.toFixed(1)}g</Text>
          <Text style={{ color: sugarColor }}>Sugar: {localSugar.toFixed(1)}g</Text>
        </div>
      </div>
      
      <List
        header={<Text strong>Dishes Consumed</Text>}
        dataSource={data.items}
        renderItem={(item) => (
          <List.Item style={{ padding: '8px 0' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
              <Text style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%' }}>{item.name}</Text>
            </div>
          </List.Item>
        )}
      />
    </Card>
  );
};

// --- Main Component ---

const Nutrition = () => {
  // --- States ---
  const [mealData, setMealData] = useState(getInitialMealData); // Structured data for the 4 meal cards
  const [totals, setTotals] = useState({ fibre: 0, carbs: 0, fat: 0, sugar: 0 }); // Aggregated daily totals
  const [isLoading, setIsLoading] = useState(true); // Loading state for initial fetch
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [currentMealType, setCurrentMealType] = useState(null);
  const [form] = Form.useForm();
  const [addDishLoading, setAddDishLoading] = useState(false);
  const [isHistoryModalVisible, setIsHistoryModalVisible] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyData, setHistoryData] = useState([]);
  
  const MOCK_GOALS = {
    fibre: 30,
    carbs: 250,
    fat: 70,
    sugar: 50,
  };

  // --- Date Calculation ---
  const today = new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });


  // --- API: Fetch Meal Data (Core Logic) ---

  const fetchMealData = async (mealType) => {
    try {
      const response = await fetch(
        `${API_BASE_URL}MealTypes/${mealType}`, 
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
        return [];
      }

      if (!response.ok) {
        console.error(`Failed to fetch ${mealType} data: ${response.statusText}`);
        return [];
      }

      return response.json();
    } catch (error) {
      console.error(`API Error fetching ${mealType} logs:`, error);
      return [];
    }
  };


  const fetchDailyMealSummaries = async () => {
    setIsLoading(true);
    let dailyTotals = { fibre: 0, carbs: 0, fat: 0, sugar: 0 };
    const newMealData = getInitialMealData();

    try {
      const fetchPromises = MEAL_TYPES_TO_FETCH.map(type => fetchMealData(type));
      const results = await Promise.all(fetchPromises);

      results.forEach((mealLogs, index) => {
        const mealType = MEAL_TYPES_TO_FETCH[index];
        // Convert BREAKFAST -> Breakfast for the map key
        const mealKey = mealType.charAt(0).toUpperCase() + mealType.slice(1).toLowerCase(); 

        if (newMealData[mealKey]) {
          mealLogs.forEach(log => {
            const fibre = log.totalFiber || 0;
            const carbs = log.totalCarbohydrates || 0;
            const fat = log.totalFat || 0;
            const sugar = log.totalSugar || 0;

            // 1. Accumulate daily totals
            dailyTotals.fibre += fibre;
            dailyTotals.carbs += carbs;
            dailyTotals.fat += fat;
            dailyTotals.sugar += sugar;

            // 2. Group for meal cards
            newMealData[mealKey].items.push({
              name: log.foodName,
              fibre,
              carbs,
              fat,
              sugar,
            });
          });
        }
      });
      
      setMealData(newMealData);
      setTotals(dailyTotals);

    } catch (error) {
      message.error("Failed to load daily nutrition data. Check the backend connection.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDailyMealSummaries();
  }, []); // Run only once on mount


  // --- Handlers ---

  const showAddDishModal = (mealType) => {
    setCurrentMealType(mealType);
    setIsModalVisible(true);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
    form.resetFields();
  };

  // --- API: Add Dish ---
  const handleAddDish = async (values) => {
    setAddDishLoading(true);
    
    // Construct the request body using the required structure (hardcoded ingredients and userId)
    const mealTypeUpper = currentMealType.toUpperCase();
    
    // Hardcode the complex ingredients structure as requested
    const ingredientsArray = [
        {
          "additionalProp1": "string", 
          "additionalProp2": "string",
          "additionalProp3": "string"
        }
    ];
    
    const requestBody = {
      recipeId: values.recipeId,
      recipeName: values.recipeName,
      ingredients: ingredientsArray, // Hardcoded ingredients array
      mealType: mealTypeUpper,
      userId: 0 // Hardcoded userId
    };

    try {
      const response = await fetch(
        `${API_BASE_URL}`, 
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
          body: JSON.stringify(requestBody), 
        }
      );

      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
        return;
      }

      if (!response.ok) {
        // Attempt to read error message from response body
        const errorText = await response.text();
        throw new Error(`Failed to add dish: ${errorText}`);
      }

      // Success: Immediately refresh the meal summaries to update the UI
      message.success(`${values.recipeName} added to ${currentMealType}!`);
      fetchDailyMealSummaries(); 
      setIsModalVisible(false);
      form.resetFields();

    } catch (error) {
      console.error("API Error during dish addition:", error);
      message.error(`Failed to add dish. Error: ${error.message}`);
      setIsModalVisible(false);
      form.resetFields();
    } finally {
        setAddDishLoading(false);
    }
  };

  // --- API: View History (Uses the previous endpoint for full log history) ---
  const handleViewHistory = async () => {
    setHistoryLoading(true);

    try {
      const response = await fetch(
        `${API_BASE_URL}NutritionLogsByUserId`, 
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
        return;
      }

      if (!response.ok) {
        throw new Error("Failed to fetch history");
      }

      const data = await response.json();
      setHistoryData(data.map(log => ({
        ...log,
        date: log.analyzedAt, // Use the analyzedAt field for date
        fibre: log.totalFiber || 0,
        carbs: log.totalCarbohydrates || 0,
        fat: log.totalFat || 0,
        sugar: log.totalSugar || 0,
      })));
      setIsHistoryModalVisible(true);

    } catch (error) {
      console.warn("API Error (Expected in Preview):", error);
      message.error("Failed to load nutrition history. Please ensure the backend is running and accessible.");
    } finally {
      setHistoryLoading(false);
    }
  };

  const historyColumns = [
    { title: 'Date', dataIndex: 'date', key: 'date' },
    { title: 'Dish', dataIndex: 'foodName', key: 'foodName' },
    { title: 'Meal Type', dataIndex: 'mealType', key: 'mealType' },
    { title: 'Fibre (g)', dataIndex: 'fibre', key: 'fibre', render: (val) => <Text style={{color: fibreColor}}>{val.toFixed(1)}</Text> },
    { title: 'Carbs (g)', dataIndex: 'carbs', key: 'carbs', render: (val) => <Text style={{color: carbsColor}}>{val.toFixed(1)}</Text> },
    { title: 'Fat (g)', dataIndex: 'fat', key: 'fat', render: (val) => <Text style={{color: fatColor}}>{val.toFixed(1)}</Text> },
    { title: 'Sugar (g)', dataIndex: 'sugar', key: 'sugar', render: (val) => <Text style={{color: sugarColor}}>{val.toFixed(1)}</Text> },
  ];

  return (
    <div style={{ padding: '16px 32px', backgroundColor: '#f0f2f5', minHeight: '100vh', fontFamily: 'sans-serif' }}>
      <LoggedInNavbar activeKey="nutrition" />
      
      {/* Header */}
      <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center', 
          marginBottom: '24px', 
          flexWrap: 'wrap', 
          gap: '16px',
          backgroundColor: PRIMARY_BG_COLOR,
          padding: '16px 24px', 
          borderRadius: '8px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
      }}>
        <Title level={2} style={{ margin: 0, color: '#262626', display: 'flex', alignItems: 'center' }}>
          <Dumbbell style={{ marginRight: '10px', color: PRIMARY_COLOR }} />
          Macro Tracker Overview
        </Title>
        <Button 
            type="primary" 
            icon={<Calendar style={{ width: '16px', height: '16px' }} />}
            onClick={handleViewHistory}
            loading={historyLoading}
        >
            View History
        </Button>
      </div>
      
      {/* 1. Daily Total Nutrition Summary */}
      <Card 
        bordered={false}
        loading={isLoading}
        title={
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Title level={4} style={{ margin: 0, color: '#262626', display: 'flex', alignItems: 'center' }}>
              <Dumbbell style={{ marginRight: '8px', color: PRIMARY_COLOR }} /> Daily Macro Breakdown
            </Title>
            {/* Display Today's Date */}
            <Text strong style={{ color: PRIMARY_COLOR, fontSize: '16px' }}>{today}</Text> 
          </div>
        }
        style={{ 
          boxShadow: '0 8px 24px rgba(0, 0, 0, 0.10)',
          marginBottom: '32px', 
          backgroundColor: '#ffffff',
          borderRadius: '8px',
          borderTop: `4px solid ${PRIMARY_COLOR}`
        }}
      >
        <Row gutter={[24, 24]}>
          <Col xs={24} sm={12} lg={6}>
            <MacroGoalProgress label="Fibre" current={totals.fibre} goal={MOCK_GOALS.fibre} unit="g" color={fibreColor} loading={isLoading} />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <MacroGoalProgress label="Carbohydrates" current={totals.carbs} goal={MOCK_GOALS.carbs} unit="g" color={carbsColor} loading={isLoading} />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <MacroGoalProgress label="Fat" current={totals.fat} goal={MOCK_GOALS.fat} unit="g" color={fatColor} loading={isLoading} />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <MacroGoalProgress label="Sugar" current={totals.sugar} goal={MOCK_GOALS.sugar} unit="g" color={sugarColor} loading={isLoading} />
          </Col>
        </Row>
      </Card>
      
      {/* 2. Meal Breakdown */}
      <Row gutter={[24, 24]}>
        {Object.entries(mealData).map(([mealName, mealData]) => (
          <Col key={mealName} xs={24} sm={12} lg={6}> 
            <MealCard
              title={mealName}
              data={mealData}
              icon={mealData.icon}
              bgColor={mealData.bgColor}
              onAddClick={showAddDishModal}
              loading={isLoading}
            />
          </Col>
        ))}
      </Row>

      {/* 3. Add Dish Modal */}
      <Modal
        title={`Add Dish to ${currentMealType}`}
        open={isModalVisible}
        onCancel={handleCancel}
        footer={null}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleAddDish}
          initialValues={{ recipeId: 1 }}
        >
          <Form.Item
            name="recipeId"
            label="Dish ID"
            rules={[{ required: true, message: 'Please enter a numeric Dish ID' }]}
          >
            <InputNumber min={1} style={{ width: '100%' }} placeholder="e.g., 4" />
          </Form.Item>
          
          <Form.Item
            name="recipeName"
            label="Dish Name"
            rules={[{ required: true, message: 'Please enter dish name' }]}
          >
            <Input placeholder="e.g., Curd Rice" />
          </Form.Item>

          {/* Hardcoded ingredients and userId are handled internally in handleAddDish */}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '16px' }}>
            <Button onClick={handleCancel}>Cancel</Button>
            <Button type="primary" htmlType="submit" loading={addDishLoading}>
              Add Dish
            </Button>
          </div>
        </Form>
      </Modal>

      {/* 4. View History Modal */}
      <Modal
        title="Nutrition History"
        open={isHistoryModalVisible}
        onCancel={() => setIsHistoryModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setIsHistoryModalVisible(false)}>
            Close
          </Button>
        ]}
        width={700}
      >
        <Table 
            dataSource={historyData} 
            columns={historyColumns} 
            pagination={{ pageSize: 5 }} 
            size="middle"
        />
      </Modal>

    </div>
  );
};

export default Nutrition;