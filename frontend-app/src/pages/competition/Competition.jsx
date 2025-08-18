function Competition() {
  return (
    <>
      <div className="skeleton h-9 w-32 mx-auto my-2">
        <h1 className="text-center text-2xl">竞赛考核</h1>
      </div>

      <div className="overflow-x-auto w-1/2 mx-auto">
        <table className="table">
          <tbody>
            {/* row 1 */}
            <tr>
              <td>
                <div className="flex items-center gap-3">
                  <div>
                    <div className="font-bold">12312 (BBB)</div>
                    <div className="text-sm opacity-50">
                      2025-08-03 20:06 - 2025-08-31 20:06
                      <button className="btn btn-success btn-xs mx-4">
                        进行中
                      </button>
                    </div>
                  </div>
                </div>
              </td>

              <th className="text-right">
                <button className="btn btn-outline btn-info">修改</button>
              </th>
            </tr>
          </tbody>
        </table>
      </div>
    </>
  );
}
export default Competition;
