import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage.jsx';
import TipPage from './pages/TipPage.jsx';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/tip" element={<TipPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
