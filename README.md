# ReelsApplication

This is an implementation of instagram-like stories. This project has two branches... run the demo branch to see how it works... to implement it into your project, just add the master branch in your project as a module, and when you need to show reels, make a list of string with urls and pass them to activity as intent with following code:

<code>val reels = arrayListOf(
                "https://user-images.githubusercontent.com/90382113/170887700-e405c71e-fe31-458d-8572-aea2e801eecc.mp4",
                "https://user-images.githubusercontent.com/90382113/170890384-43214cc8-79c6-4815-bcb7-e22f6f7fe1bc.mp4",
                "https://user-images.githubusercontent.com/90382113/170889265-7ed9a56c-dd5f-4d78-b453-18b011644da0.mp4",
                "https://user-images.githubusercontent.com/90382113/170885742-d82e3b59-e45a-4fcf-a851-fed58ff5a690.mp4",
            )
  val intent = Intent(this,ReelActivity::class.java)
  intent.putStringArrayListExtra("data",reels)
  startActivity(intent)
</code>

<H2>Screeenshots</H2>
<img src="https://github.com/Fareed-ud-Din-Munawwar/ReelsApplication/assets/64327426/41c4f49e-19c9-4737-860e-12ee065960c3" width="300" height="700">
<img src="https://github.com/Fareed-ud-Din-Munawwar/ReelsApplication/assets/64327426/d8bfa981-c4e3-4393-8797-98c142b4b61a" width="300" height="700">
<img src="https://github.com/Fareed-ud-Din-Munawwar/ReelsApplication/assets/64327426/7269602d-dd36-45ce-a0a1-b018b36b8d96" width="300" height="700">
<img src="https://github.com/Fareed-ud-Din-Munawwar/ReelsApplication/assets/64327426/64d380da-a329-4e53-8805-0742ca36af9b" width="300" height="700">
<img src="https://github.com/Fareed-ud-Din-Munawwar/ReelsApplication/assets/64327426/081cdb8b-adbd-4397-af19-e0aa95bc85a3" width="300" height="700">
