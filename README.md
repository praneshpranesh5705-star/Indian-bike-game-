# Indian Bike Game — AI Integration

This repository contains the secure backend contract and Unity integration kit for adding an AI Assistant to the Indian Bike Game.

## Planned features

- Ask the AI which bike is suitable for a situation.
- Generate driving challenges dynamically.
- Return short, game-friendly answers.
- Keep the OpenAI API key on the server, never inside the APK.

## Important

The currently available game file is a compiled APK, not the original Unity project. The Unity project/source is still required to actually add the C# component and rebuild the game APK.

## Backend

The `/api/ai` endpoint expects:

```json
{"message":"Give me a driving challenge"}
```

and returns:

```json
{"reply":"..."}
```

Set `OPENAI_API_KEY` as a server-side environment variable before deploying the endpoint.
