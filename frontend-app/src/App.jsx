import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

import LayoutApp from "./ui/LayoutApp";
import QuestionList from "./pages/question-list/QuestionList";
import Competition from "./pages/competition/Competition";
import UserList from "./pages/user-list/UserList";
import Login from "./pages/auth/Login";
import Signup from "./pages/auth/Signup";
import Home from "./ui/Home";
import Startup from "./ui/Startup";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LayoutApp />}>
          <Route path="home" element={<Home />}>
            <Route path="" element={<Navigate to="/home/question-list" />} />
            <Route path="question-list" element={<QuestionList />} />
            <Route path="competition" element={<Competition />} />
            <Route path="user-list" element={<UserList />} />
          </Route>
          <Route path="startup" element={<Startup />} />
          <Route path="" element={<Navigate to="/startup" />} />
          <Route path="login" element={<Login />} />
          <Route path="signup" element={<Signup />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
export default App;
