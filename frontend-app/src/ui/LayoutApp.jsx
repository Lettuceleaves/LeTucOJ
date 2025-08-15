import { Outlet } from "react-router-dom";
import NavBar from "./NavBar";

function LayoutApp() {
  return (
    <>
      <Outlet />
    </>
  );
}
export default LayoutApp;
