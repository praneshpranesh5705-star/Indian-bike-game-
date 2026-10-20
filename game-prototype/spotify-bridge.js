// Spotify bridge for the browser game.
// On Android, Spotify's spotify: URI opens the installed Spotify app.
// After the user starts music in Spotify and returns to the game, the
// Spotify app can continue playing in the background.
(function(){
  const spotifyUri='spotify:playlist:2xyXwUtwQh3E8fsGbeJyRQ';
  const spotifyWeb='https://open.spotify.com/playlist/2xyXwUtwQh3E8fsGbeJyRQ?utm_source=indian-bike-ai';
  const button=document.getElementById('openSpotify');
  const phone=document.getElementById('phone');
  const state=document.getElementById('musicState');
  const toastEl=document.getElementById('toast');

  function toast(message){
    if(!toastEl)return;
    toastEl.textContent=message;
    toastEl.style.display='block';
    clearTimeout(window.spotifyToastTimer);
    window.spotifyToastTimer=setTimeout(()=>toastEl.style.display='none',2600);
  }

  function closePhone(){
    if(!phone)return;
    phone.style.display='none';
    window.dispatchEvent(new Event('spotifyGameResume'));
  }

  function markReady(){
    localStorage.setItem('indianBikeSpotifyReady','1');
    localStorage.setItem('indianBikeSpotifyStartedAt',String(Date.now()));
  }

  function openSpotify(){
    markReady();
    if(state) state.textContent='Spotify opened • Start the playlist, then return to the game';
    closePhone();
    toast('🎵 Spotify opening — start music, then return to the game');

    // Android's registered spotify: scheme gives the installed Spotify app
    // priority over the browser. If it is unavailable, fall back to the web app.
    let fallback=setTimeout(()=>{ window.location.href=spotifyWeb; },1400);
    const cancelFallback=()=>{clearTimeout(fallback);window.removeEventListener('pagehide',cancelFallback)};
    window.addEventListener('pagehide',cancelFallback,{once:true});
    try{ window.location.href=spotifyUri; }
    catch(e){ window.location.href=spotifyWeb; }
  }

  if(button){
    button.textContent='▶ Play in Spotify & Return';
    button.onclick=openSpotify;
  }

  // When the user comes back from Spotify, restore the game-facing message.
  window.addEventListener('pageshow',()=>{
    if(localStorage.getItem('indianBikeSpotifyReady')==='1'){
      if(state) state.textContent='🎵 Spotify music ready • You can drive now';
      toast('🎵 Welcome back — drive while Spotify plays');
    }
  });

  window.addEventListener('spotifyGameResume',()=>{
    if(state && localStorage.getItem('indianBikeSpotifyReady')==='1')
      state.textContent='🎵 Spotify music ready • You can drive now';
  });
})();
