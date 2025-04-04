import React, { useState } from 'react';
import { Box, Tabs, Tab } from '@mui/material';
import ReviewList from './ReviewList';
import CrawlerDashboard from './CrawlerDashboard';

function TabPanel({ children, value, index }) {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`tabpanel-${index}`}
      aria-labelledby={`tab-${index}`}
    >
      {value === index && <Box>{children}</Box>}
    </div>
  );
}

const MainDashboard = () => {
  const [currentTab, setCurrentTab] = useState(0);

  const handleTabChange = (event, newValue) => {
    setCurrentTab(newValue);
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={currentTab} onChange={handleTabChange}>
          <Tab label="Reviews" />
          <Tab label="Crawler Dashboard" />
        </Tabs>
      </Box>
      
      <TabPanel value={currentTab} index={0}>
        <ReviewList />
      </TabPanel>
      <TabPanel value={currentTab} index={1}>
        <CrawlerDashboard />
      </TabPanel>
    </Box>
  );
};

export default MainDashboard; 