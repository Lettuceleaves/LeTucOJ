function Startup() {
  return (
    <>
      <div
        className="hero min-h-screen"
        style={{
          backgroundImage:
            "url(https://img.daisyui.com/images/stock/photo-1507358522600-9f71e620c44e.webp)",
        }}
      >
        <div className="hero-overlay"></div>
        <div className="hero-content text-neutral-content text-center">
          <div className="max-w-md">
            <div className="-mt-10">
              <h1 className="mb-5 text-5xl font-bold">LetucOJ</h1>
              <p className="mb-5">一个试图让出题变简单的 OJ 系统</p>
            </div>
            <div className="tooltip mt-8">
              <div className="tooltip-content bg-clip-text">
                <div className="animate-bounce text-black -rotate-10 text-2xl font-black ">
                  Go!
                </div>
              </div>
              <button className="btn btn-ghost hover:scale-[1.2] bg-clip-text font-sans-serif">
                Start up ➡️
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* <div className="flex justify-center mt-40">
        <div className="tooltip">
          <div className="tooltip-content">
            <div className="animate-bounce text-orange-400 -rotate-10 text-2xl font-black">
              Go!
            </div>
          </div>
          <button className="btn">Start up ➡️</button>
        </div>
      </div> */}
    </>
  );
}
export default Startup;
