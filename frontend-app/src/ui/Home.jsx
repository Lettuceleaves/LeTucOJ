import { Outlet } from "react-router-dom";
import NavBar from "./NavBar";

function Home() {
  return (
    <>
      <NavBar />

      <Outlet />
    </>
  );
}
export default Home;
