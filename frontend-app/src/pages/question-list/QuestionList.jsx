function QuestionList() {
  return (
    <>
      <div className="skeleton h-9 w-32 mx-auto my-2">
        <h1 className="text-center text-2xl">竞赛考核</h1>
      </div>

      <div className="grid grid-cols-3">
        <label className="input">
          <svg
            className="h-[1em] opacity-50"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
          >
            <g
              strokeLinejoin="round"
              strokeLinecap="round"
              strokeWidth="2.5"
              fill="none"
              stroke="currentColor"
            >
              <circle cx="11" cy="11" r="8"></circle>
              <path d="m21 21-4.3-4.3"></path>
            </g>
          </svg>
          <input type="search" required placeholder="Search" />
        </label>
      </div>

      <div>
        <select
          defaultValue="Pick a text editor"
          className="select select-primary"
        >
          <option disabled={true}>By Sort</option>
          <option>按题目名称</option>
          <option>按测试用例数量</option>
        </select>
      </div>
    </>
  );
}
export default QuestionList;
