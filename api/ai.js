export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');

  if (req.method === 'OPTIONS') return res.status(204).end();
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'POST only' });
  }

  const apiKey = process.env.OPENAI_API_KEY;
  if (!apiKey) {
    return res.status(500).json({ error: 'OPENAI_API_KEY is not configured on the server.' });
  }

  const message = typeof req.body?.message === 'string' ? req.body.message.trim() : '';
  if (!message) return res.status(400).json({ error: 'message is required' });
  if (message.length > 1000) return res.status(400).json({ error: 'message is too long' });

  const instructions = `You are Bike AI, an in-game assistant for Indian Bikes Driving 3D.
Give concise, fun, safe answers for a mobile driving game.
You can recommend bike types, explain game strategy, and create fictional driving challenges.
When asked for a challenge, include: title, bike recommendation, time limit, 2-4 objectives, difficulty, and reward.
Do not claim access to the player's actual game state unless it is included in the message.
Do not provide real-world dangerous driving instructions; keep challenges fictional and game-focused.`;

  try {
    const response = await fetch('https://api.openai.com/v1/responses', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${apiKey}`
      },
      body: JSON.stringify({
        model: 'gpt-5.6-luna',
        instructions,
        input: message,
        max_output_tokens: 350
      })
    });

    const data = await response.json();
    if (!response.ok) {
      console.error('OpenAI error:', data);
      return res.status(502).json({ error: 'AI service request failed' });
    }

    const reply = data.output_text ||
      data.output?.flatMap(item => item.content || [])
        ?.filter(part => part.type === 'output_text')
        ?.map(part => part.text)
        ?.join(' ') ||
      'I could not generate a response right now.';

    return res.status(200).json({ reply });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: 'Server error' });
  }
}
