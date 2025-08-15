import { useLocation, useNavigate } from "react-router-dom";

function NavBar() {
  const navgate = useNavigate();
  const location = useLocation();

  return (
    <div className="navbar bg-base-300 shadow-sm">
      <div className="navbar-start">
        <div className="dropdown">
          <div tabIndex={0} role="button" className="btn btn-ghost lg:hidden">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              {" "}
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M4 6h16M4 12h8m-8 6h16"
              />{" "}
            </svg>

            {/* Mobile menu */}
          </div>
          <ul
            tabIndex={0}
            className="menu menu-sm dropdown-content bg-base-100 rounded-box z-1 mt-3 w-52 p-2 shadow"
          >
            <li>
              <a
                className={
                  location.pathname === "/home/question-list"
                    ? "menu-active"
                    : ""
                }
                onClick={() => navgate("/home/question-list")}
              >
                题目列表
              </a>
            </li>
            <li>
              <a
                className={
                  location.pathname === "/home/competition" ? "menu-active" : ""
                }
                onClick={() => navgate("/home/competition")}
              >
                竞赛列表
              </a>
            </li>
            <li>
              <a
                className={
                  location.pathname === "/home/user-list" ? "menu-active" : ""
                }
                onClick={() => navgate("/home/user-list")}
              >
                用户列表
              </a>
            </li>
          </ul>
        </div>
        <a className="btn btn-ghost text-xl" onClick={() => navgate("/home")}>
          LeTucOJ
        </a>
      </div>

      {/* Desktop menu */}
      <div className="navbar-center hidden lg:flex">
        <ul className="menu menu-horizontal px-1">
          <li>
            <a
              className={
                location.pathname === "/home/question-list" ? "menu-active" : ""
              }
              onClick={() => navgate("/home/question-list")}
            >
              题目列表
            </a>
          </li>
          <li>
            <a
              className={
                location.pathname === "/home/competition" ? "menu-active" : ""
              }
              onClick={() => navgate("/home/competition")}
            >
              竞赛列表
            </a>
          </li>
          <li>
            <a
              className={
                location.pathname === "/home/user-list" ? "menu-active" : ""
              }
              onClick={() => navgate("/home/user-list")}
            >
              用户列表
            </a>
          </li>
        </ul>
      </div>
      <div class="navbar-end">
        <div class="dropdown dropdown-end">
          <div
            tabIndex="0"
            role="button"
            class="btn btn-ghost btn-circle avatar"
          >
            <div class="w-10 rounded-full">
              <img
                alt="Tailwind CSS Navbar component"
                src="https://img.daisyui.com/images/stock/photo-1534528741775-53994a69daeb.webp"
              />
            </div>
          </div>
          <ul
            tabIndex="0"
            class="menu menu-sm dropdown-content bg-base-100 rounded-box z-1 mt-3 w-52 p-2 shadow"
          >
            <li>
              <a class="justify-between">
                Profile
                <span class="badge">New</span>
              </a>
            </li>
            <li>
              <a>Settings</a>
            </li>
            <li>
              <a>Logout</a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}
export default NavBar;
