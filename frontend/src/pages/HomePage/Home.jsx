import "./Home.css"
import LoggedInNavbar from "../../components/Navbar/LoggedInNavbar/LoggedInNavbar";

import { Tag } from 'antd';
import { HeartOutlined, MessageFilled, ShareAltOutlined } from '@ant-design/icons';

const Home = () => {
    return(
        <>
        <LoggedInNavbar activeKey="1"/>
        <div className="main-content">
            <div className="main-grid">
                
                {/* ----------------------------Left Bar---------------------------------- */}
                <div className="left-sidebar-column">
                    <div className="profile-card">
                        <div className="profile-header"></div>
                        <div className="profile-content">
                            <div className="profile-avatar">S</div>
                            <div className="profile-name">Sarah Baker</div>
                            <div className="profile-title">Home Cook & Food Blogger</div>
                        
                            <div className="profile-stats">
                                <div className="stat-item">
                                    <span className="stat-number">125</span>
                                    <span className="stat-label">Recipes Saved</span>
                                </div>
                                <div className="stat-item">
                                    <span className="stat-number">2.3K</span>
                                    <span className="stat-label">Followers</span>
                                </div>
                            </div>
                        
                            <a href="#" className="profile-link" onclick="navigate('View Full Profile'); return false;">
                                <i class="fas fa-user-circle"></i> View Profile
                            </a>
                            <a href="#" className="profile-link" onclick="navigate('My Saved Recipes'); return false;">
                                <i class="fas fa-bookmark"></i> My Cookbook
                            </a>
                        </div>
                    </div>
                </div>

                {/* ----------------------------Recipe Card------------------------------- */}
                <div className="feed-column">
                    <div className="feed-controls">
                        <div className="search-container">
                            <input type="text" placeholder="Search recipes, users, or tags..." className="search-bar" onfocus="showMessage('Mock search activated.');"/>
                        </div>
                        <button className="feed-post-btn" onclick="openPostModal();">
                            <i class="fas fa-upload"></i> Share Recipe
                        </button>
                    </div>
                
                    <h2>Latest Community Recipes</h2>

                    <div className="recipe-card" data-id="1">
                        <img src="https://placehold.co/800x600/ff7f50/ffffff?text=Vibrant+Salmon+Recipe" onerror="this.onerror=null; this.src='https://placehold.co/800x600/ccc/333?text=Image+Load+Failed';" alt="Spicy Sriracha Salmon Bowls" className="recipe-image"/>
                        <div className="card-content">
                            <div className="card-author-info">
                                <div className="author-avatar">CA</div>
                                <div>
                                    <h3>Spicy Sriracha Salmon Bowls</h3>
                                    <div className="author-name">Posted by <a href="#" onclick="navigate('Profile'); return false;">@Chef_Alex</a></div>
                                </div>
                            </div>
                            <p className="recipe-description">A quick and delicious weeknight dinner with a fiery kick. Ready in under 30 minutes!</p>
                            <div>
                                <Tag color="#f50">orange</Tag>
                            </div>
                            <div className="card-divider"></div>
                            <div className="card-actions">
                                <button className="action-button like-button" onclick="likeRecipe(1, this)">
                                   <HeartOutlined />
                                    <span id="like-count-1">154</span>
                                </button>
                                <button className="action-button" onclick="navigate('View Comments');">
                                    <MessageFilled />
                                    28
                                </button>
                                <button className="action-button" onclick="navigate('Share');">
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
                        <h4><i class="fas fa-fire" ></i> Cooking Challenges</h4>
                        <ul className="challenge-list">
                            <li className="challenge-item">
                                <div className="item-details">
                                    <div className="icon"><i class="fas fa-award"></i></div>
                                    <div>
                                        <div className="item-title">The Perfect Pasta Challenge</div>
                                        <div className="item-meta">Theme: Comfort Food</div>
                                    </div>
                                </div>
                                <button className="join-btn" onclick="showMessage('Joined Perfect Pasta Challenge!');">Join</button>
                            </li>
                            <li className="challenge-item">
                                <div className="item-details">
                                    <div className="icon"><i class="fas fa-leaf"></i></div>
                                    <div>
                                        <div className="item-title">Veganuary: Green Cuisine</div>
                                        <div className="item-meta">Theme: Plant-Based</div>
                                    </div>
                                </div>
                                <button className="join-btn" onclick="showMessage('Joined Veganuary Challenge!');">Join</button>
                            </li>
                        </ul>
                        <a href="#" className="view-all" onclick="navigate('View All Challenges'); return false;">View All Challenges</a>
                    </div>

                    <div className="sidebar-card winner-card">
                        <h4><i class="fas fa-trophy" ></i> Recent Winners</h4>
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
        </>
    )
}

export default Home;