using System;
using System.Collections;
using TMPro;
using UnityEngine;
using UnityEngine.Networking;
using UnityEngine.UI;

public class BikeAIChat : MonoBehaviour
{
    [Header("Deploy your Vercel API and paste its URL here")]
    [SerializeField] private string apiUrl = "https://YOUR-VERCEL-DOMAIN.vercel.app/api/ai";

    private GameObject panel;
    private TMP_InputField input;
    private TMP_Text output;
    private Button sendButton;

    private void Start()
    {
        BuildUi();
    }

    private void BuildUi()
    {
        var canvasObject = new GameObject("BikeAI_Canvas");
        var canvas = canvasObject.AddComponent<Canvas>();
        canvas.renderMode = RenderMode.ScreenSpaceOverlay;
        canvasObject.AddComponent<CanvasScaler>();
        canvasObject.AddComponent<GraphicRaycaster>();

        var openButtonObject = CreateButton(canvasObject.transform, "AI ASSISTANT", new Vector2(0.86f, 0.12f), new Vector2(240, 80));
        openButtonObject.GetComponent<Button>().onClick.AddListener(() => panel.SetActive(true));

        panel = new GameObject("BikeAI_Panel");
        panel.transform.SetParent(canvasObject.transform, false);
        var image = panel.AddComponent<Image>();
        image.color = new Color(0.03f, 0.04f, 0.07f, 0.96f);
        var panelRect = panel.GetComponent<RectTransform>();
        panelRect.anchorMin = new Vector2(0.08f, 0.18f);
        panelRect.anchorMax = new Vector2(0.92f, 0.82f);
        panelRect.offsetMin = Vector2.zero;
        panelRect.offsetMax = Vector2.zero;

        var close = CreateButton(panel.transform, "CLOSE", new Vector2(0.9f, 0.9f), new Vector2(150, 60));
        close.GetComponent<Button>().onClick.AddListener(() => panel.SetActive(false));

        var title = CreateText(panel.transform, "🏍️ BIKE AI", 40, new Vector2(0.5f, 0.9f), new Vector2(600, 70));
        title.alignment = TextAlignmentOptions.Center;

        input = CreateInput(panel.transform, "Ask: Which bike should I use? / Give me a driving challenge", new Vector2(0.5f, 0.55f), new Vector2(900, 140));
        output = CreateText(panel.transform, "Ask Bike AI anything about your game.", 28, new Vector2(0.5f, 0.72f), new Vector2(900, 170));
        output.alignment = TextAlignmentOptions.TopLeft;

        sendButton = CreateButton(panel.transform, "ASK AI", new Vector2(0.5f, 0.34f), new Vector2(260, 80)).GetComponent<Button>();
        sendButton.onClick.AddListener(SendMessageToAI);

        panel.SetActive(false);
    }

    private void SendMessageToAI()
    {
        if (string.IsNullOrWhiteSpace(input.text)) return;
        StartCoroutine(CallAI(input.text.Trim()));
    }

    private IEnumerator CallAI(string message)
    {
        sendButton.interactable = false;
        output.text = "AI is thinking...";

        string json = JsonUtility.ToJson(new ChatRequest { message = message });
        using (var request = new UnityWebRequest(apiUrl, "POST"))
        {
            byte[] body = System.Text.Encoding.UTF8.GetBytes(json);
            request.uploadHandler = new UploadHandlerRaw(body);
            request.downloadHandler = new DownloadHandlerBuffer();
            request.SetRequestHeader("Content-Type", "application/json");
            request.timeout = 20;

            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                output.text = "AI connection failed. Check your internet connection and API URL.";
            }
            else
            {
                try
                {
                    var reply = JsonUtility.FromJson<ChatResponse>(request.downloadHandler.text);
                    output.text = string.IsNullOrEmpty(reply.reply) ? "No AI response." : reply.reply;
                }
                catch
                {
                    output.text = "Invalid AI response.";
                }
            }
        }

        sendButton.interactable = true;
    }

    private static TMP_Text CreateText(Transform parent, string text, float size, Vector2 anchor, Vector2 sizeDelta)
    {
        var go = new GameObject("Text");
        go.transform.SetParent(parent, false);
        var tmp = go.AddComponent<TextMeshProUGUI>();
        tmp.text = text;
        tmp.fontSize = size;
        tmp.color = Color.white;
        var rect = go.GetComponent<RectTransform>();
        rect.anchorMin = rect.anchorMax = anchor;
        rect.sizeDelta = sizeDelta;
        return tmp;
    }

    private static GameObject CreateButton(Transform parent, string text, Vector2 anchor, Vector2 size)
    {
        var go = new GameObject("Button_" + text);
        go.transform.SetParent(parent, false);
        var image = go.AddComponent<Image>();
        image.color = new Color(0.08f, 0.5f, 1f, 1f);
        var button = go.AddComponent<Button>();
        var label = CreateText(go.transform, text, 24, new Vector2(0.5f, 0.5f), size);
        label.alignment = TextAlignmentOptions.Center;
        var rect = go.GetComponent<RectTransform>();
        rect.anchorMin = rect.anchorMax = anchor;
        rect.sizeDelta = size;
        return go;
    }

    private static TMP_InputField CreateInput(Transform parent, string placeholder, Vector2 anchor, Vector2 size)
    {
        var go = new GameObject("AI_Input");
        go.transform.SetParent(parent, false);
        var image = go.AddComponent<Image>();
        image.color = new Color(1f, 1f, 1f, 0.12f);
        var field = go.AddComponent<TMP_InputField>();
        var text = CreateText(go.transform, "", 24, new Vector2(0.5f, 0.5f), size);
        text.alignment = TextAlignmentOptions.Left;
        field.textComponent = text;
        var rect = go.GetComponent<RectTransform>();
        rect.anchorMin = rect.anchorMax = anchor;
        rect.sizeDelta = size;
        return field;
    }

    [Serializable] private class ChatRequest { public string message; }
    [Serializable] private class ChatResponse { public string reply; }
}
