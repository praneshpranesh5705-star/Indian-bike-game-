# AI integration — Unity project

1. Open the original Unity project.
2. Copy `Unity/Assets/Scripts/BikeAI/BikeAIChat.cs` from this repository into `Assets/Scripts/BikeAI/` in the real Unity project.
3. Add an empty GameObject to the first scene and attach `BikeAIChat`.
4. Deploy the `/api/ai` function to Vercel.
5. Set the Vercel environment variable `OPENAI_API_KEY` to your OpenAI project key.
6. Put the deployed URL into the component's `apiUrl`, for example `https://your-project.vercel.app/api/ai`.
7. Build a new Android APK from Unity.

The API key must remain on the server. Never hard-code it in the Unity app.

The runtime UI creates an **AI ASSISTANT** button. It can answer questions such as:

- Which bike should I use?
- Give me a driving challenge.
- What should I do next?

The current APK alone cannot be rebuilt from this script because an APK is a compiled Unity build; the original Unity project is still required for the final in-game integration.
