package com.example.syncdrawing;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.example.syncdrawing.packet.PacketID;
import com.example.syncdrawing.view.CustomWebview;

public class MainFragment extends Fragment {

	private FrameLayout mContentView;
	private FrameLayout mCustomViewContainer;
	private FrameLayout mBrowserFrameLayout;
	private View mCustomView;
	private MyWebChromeClient mWebChromeClient;

	private MyWebViewClient mWebClient;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	private CustomWebview mWebview;
	private IMainFragmentListener mListener = null;
	private String currentUrl = "";

	private VideoView mVideo = null;
	public interface IMainFragmentListener {
		void onSendUrl(String pString, String pUserAgent, short pEventId);
	}
	public MainFragment(IMainFragmentListener pListener) {
		mListener = pListener;

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mBrowserFrameLayout = (FrameLayout) inflater.inflate(R.layout.fragment_main, container, false);
		init();
		return mBrowserFrameLayout;
	}

	private void init() {

		mWebview = new CustomWebview(getActivity());
		mContentView = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.main_content);
		// mWebview = (CustomWebview)
		// mBrowserFrameLayout.findViewById(R.id.cbWebview);
		mCustomViewContainer = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.fullscreen_custom_content);

		mWebChromeClient = new MyWebChromeClient();
		mWebClient = new MyWebViewClient();

		mWebview.setWebChromeClient(mWebChromeClient);
		mWebview.setWebViewClient(mWebClient);

		// Configure the webview
		WebSettings settings = mWebview.getSettings();
		settings.setPluginState(PluginState.ON);
		settings.setJavaScriptEnabled(true);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		// settings.setAllowFileAccess(true);
		// settings.setAppCacheEnabled(true);
		// settings.setDomStorageEnabled(true);
		// settings.setPluginState(WebSettings.PluginState.OFF);
		// settings.setBuiltInZoomControls(true);
		// settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		// settings.setUseWideViewPort(true);
		// settings.setLoadWithOverviewMode(true);
		// settings.setSavePassword(true);
		// settings.setSaveFormData(true);
		//
		// // enable navigator.geolocation
		// settings.setGeolocationEnabled(false);
		// //
		// settings.setGeolocationDatabasePath("/data/data/com.example.vimeotest/databases/");
		//
		// // enable Web Storage: localStorage, sessionStorage
		// settings.setDomStorageEnabled(true);

		mWebview.loadUrl("https://www.youtube.com");
		// mWebview.loadUrl("rtsp://192.168.0.14/Resource/265/surfing.265");
		mContentView.addView(mWebview);
		// // String userAgent = settings.getUserAgentString();
		//
		// mVideo = (VideoView) mBrowserFrameLayout.findViewById(R.id.video);
		// // MediaController media = new MediaController(getActivity());
		// // media.setAnchorView(mVideo);
		// Uri v = Uri.parse("rtsp://175.198.74.199/Resource/265/surfing.265");
		// // mVideo.setMediaController(media);
		// mVideo.setVideoURI(v);
		// // mVideo.requestFocus();
		// mVideo.start();
		//
		// final MediaController mc = new MediaController(getActivity());
		// // mVideo.setVideoURI(v);
		// mVideo.setMediaController(mc);
		// mVideo.postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// mc.show(0);
		// }
		// }, 100);
		// mVideo.start();
		// startActivity(new Intent(Intent.ACTION_VIEW,
		// Uri.parse("rtsp://175.198.74.199/Resource/265/surfing.265")));

		// final VideoView mVideoView = (VideoView)
		// mBrowserFrameLayout.findViewById(R.id.video);
		// mVideoView.setVideoPath("rtsp://175.198.74.199/Resource/264/slamtv10.264");
		// MediaController mediaController = new MediaController(getActivity());
		// mediaController.setAnchorView(mVideoView);
		// mVideoView.setMediaController(mediaController);
		// mVideoView.requestFocus();
		//
		//
		// mVideoView.setOnPreparedListener(new OnPreparedListener() {
		// // Close the progress bar and play the video
		// public void onPrepared(MediaPlayer mp) {
		//
		// mVideoView.start();
		// }
		// });
	}

	public boolean inCustomView() {
		return (mCustomView != null);
	}

	public void hideCustomView() {
		mWebChromeClient.onHideCustomView();
	}

	public void setUrl(String pUrl, String pUserAgent) {
		if (currentUrl.compareTo(pUrl) != 0) {
			currentUrl = pUrl;
			if (pUserAgent != null && pUserAgent.length() > 0) {
				//Mozilla/5.0 (Linux; Android 5.0.2; LG-V700n Build/LRX22G; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/44.0.2403.117 Safari/537.36
				//Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2378.0 Safari/537.36
				mWebview.getSettings().setUserAgentString(pUserAgent);
			}
			mWebview.loadUrl(pUrl);
		}
	}

	public boolean onBackkeyPress() {
		if ((mCustomView == null) && mWebview.canGoBack()) {
			mWebview.goBack();
			return true;
		}
		return false;
	}

	private class MyWebChromeClient extends WebChromeClient {
		private Bitmap mDefaultVideoPoster;
		private View mVideoProgressView;

		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {

			mBrowserFrameLayout.setVisibility(View.GONE);

			// if a view already exists then immediately terminate the new
			// one
			if (mCustomView != null) {
				callback.onCustomViewHidden();
				return;
			}

			mCustomViewContainer.addView(view);
			mCustomView = view;
			mCustomViewCallback = callback;
			mCustomViewContainer.setVisibility(View.VISIBLE);
		}

		@Override
		public void onHideCustomView() {

			if (mCustomView == null)
				return;

			// Hide the custom view.
			mCustomView.setVisibility(View.GONE);

			// Remove the custom view from its container.
			mCustomViewContainer.removeView(mCustomView);
			mCustomView = null;
			mCustomViewContainer.setVisibility(View.GONE);
			mCustomViewCallback.onCustomViewHidden();

			mCustomView.setVisibility(View.VISIBLE);

		}

		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
			return true;
		}

		@Override
		public Bitmap getDefaultVideoPoster() {

			if (mDefaultVideoPoster == null) {
				mDefaultVideoPoster = BitmapFactory.decodeResource(getResources(), R.drawable.default_video_poster);
			}
			return mDefaultVideoPoster;
		}

		@Override
		public View getVideoLoadingProgressView() {

			if (mVideoProgressView == null) {
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				mVideoProgressView = inflater.inflate(R.layout.video_loading_progress, null);
			}
			return mVideoProgressView;
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {

			(getActivity()).setTitle(title);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {

			(getActivity()).getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress * 100);
		}

		@Override
		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
			callback.invoke(origin, true, false);
		}

		@Override
		public void onReceivedIcon(WebView view, Bitmap icon) {
			// TODO Auto-generated method stub
			super.onReceivedIcon(view, icon);
		}

		@Override
		public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
			// TODO Auto-generated method stub
			super.onReceivedTouchIconUrl(view, url, precomposed);
		}

		@Override
		public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
			// TODO Auto-generated method stub
			super.onShowCustomView(view, requestedOrientation, callback);
		}

		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			// TODO Auto-generated method stub
			return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
		}

		@Override
		public void onRequestFocus(WebView view) {
			// TODO Auto-generated method stub
			super.onRequestFocus(view);
		}

		@Override
		public void onCloseWindow(WebView window) {
			// TODO Auto-generated method stub
			super.onCloseWindow(window);
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			// TODO Auto-generated method stub
			return super.onJsAlert(view, url, message, result);
		}

		@Override
		public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
			// TODO Auto-generated method stub
			return super.onJsConfirm(view, url, message, result);
		}

		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
			// TODO Auto-generated method stub
			return super.onJsPrompt(view, url, message, defaultValue, result);
		}

		@Override
		public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
			// TODO Auto-generated method stub
			return super.onJsBeforeUnload(view, url, message, result);
		}

		@Override
		public void onGeolocationPermissionsHidePrompt() {
			// TODO Auto-generated method stub
			super.onGeolocationPermissionsHidePrompt();
		}

		@Override
		public boolean onJsTimeout() {
			// TODO Auto-generated method stub
			return super.onJsTimeout();
		}

		@Override
		public void onConsoleMessage(String message, int lineNumber, String sourceID) {
			// TODO Auto-generated method stub
			super.onConsoleMessage(message, lineNumber, sourceID);
		}

		@Override
		public void getVisitedHistory(ValueCallback<String[]> callback) {
			// TODO Auto-generated method stub
			super.getVisitedHistory(callback);
		}

		@Override
		public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota,
				QuotaUpdater quotaUpdater) {
			// TODO Auto-generated method stub
			super.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
		}

		@Override
		public void onReachedMaxAppCacheSize(long requiredStorage, long quota, QuotaUpdater quotaUpdater) {
			// TODO Auto-generated method stub
			super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
		}

	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			// TODO Auto-generated method stub
			super.onReceivedError(view, errorCode, description, failingUrl);

			switch (errorCode) {
			// 서버상에서 유저인증 실패
				case WebViewClient.ERROR_AUTHENTICATION :

					break;
				// 부정확안 형식의 URL
				case WebViewClient.ERROR_BAD_URL :

					break;
				// 서버로의 접속 실패
				case WebViewClient.ERROR_CONNECT :

					break;
				// SSL의 핸드쉐이크 실패
				case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE :

					break;
				// 파일 에러
				case WebViewClient.ERROR_FILE :

					break;
				// 파일 없음
				case WebViewClient.ERROR_FILE_NOT_FOUND :

					break;
				// 존재하지 않는 호스트
				case WebViewClient.ERROR_HOST_LOOKUP :

					break;
				// 서버에 대한 읽기 쓰기 실패
				case WebViewClient.ERROR_IO :

					break;
				// Proxy 서버의 인증 실패
				case WebViewClient.ERROR_PROXY_AUTHENTICATION :

					break;
				// 리다이렉트 루트가 포함됨
				case WebViewClient.ERROR_REDIRECT_LOOP :

					break;

				// 접속 타입 아웃
				case WebViewClient.ERROR_TIMEOUT :

					break;
				// 동기 리퀘스트가 너무 많음
				case WebViewClient.ERROR_TOO_MANY_REQUESTS :

					break;
				// 알수 없는 에러
				case WebViewClient.ERROR_UNKNOWN :

					break;
				// 지원하지 않는 인증방식
				case WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME :

					break;
				// 지원하지 않는 URI
				case WebViewClient.ERROR_UNSUPPORTED_SCHEME :

					break;
				default :
					break;
			}
		}
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String urlConection) {
			return false;
		}

		@Override
		public void onLoadResource(WebView view, String url) {

			super.onLoadResource(view, url);

		}

		@Override
		public void onPageFinished(WebView view, String url) {

			if (currentUrl.compareTo(url) != 0) {

				currentUrl = url;

				mListener.onSendUrl(url, view.getSettings().getUserAgentString(), PacketID.BC_BROWSER_NAVIGATE);

			}

			super.onPageFinished(view, url);
		}

		// @Override
		// public void onPageStarted(WebView view, String url, Bitmap favicon) {
		// super.onPageStarted(view, url, favicon);
		// }
		//
		//
		//
		// @Override
		// public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
		// return super.shouldOverrideKeyEvent(view, event);
		// }
		//
		// @Override
		// public WebResourceResponse shouldInterceptRequest(WebView view,
		// String url) {
		// return super.shouldInterceptRequest(view, url);
		// }
		// @Override
		// public void onTooManyRedirects(WebView view, Message cancelMsg,
		// Message continueMsg) {
		// // TODO Auto-generated method stub
		// super.onTooManyRedirects(view, cancelMsg, continueMsg);
		// }
		//

		//
		// @Override
		// public void onFormResubmission(WebView view, Message dontResend,
		// Message resend) {
		// // TODO Auto-generated method stub
		// super.onFormResubmission(view, dontResend, resend);
		// }
		//
		// @Override
		// public void doUpdateVisitedHistory(WebView view, String url, boolean
		// isReload) {
		// // TODO Auto-generated method stub
		// super.doUpdateVisitedHistory(view, url, isReload);
		// }
		//
		// @Override
		// public void onReceivedSslError(WebView view, SslErrorHandler handler,
		// SslError error) {
		// // TODO Auto-generated method stub
		// super.onReceivedSslError(view, handler, error);
		// }
		//
		// @Override
//		 public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler
//		 handler, String host, String realm) {
//		 // TODO Auto-generated method stub
////			 super.onReceivedHttpAuthRequest(view, handler, host, realm);
//			 handler.proceed("guest4", "1234");
//		 }
		//
		// @Override
		// public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
		// // TODO Auto-generated method stub
		// super.onUnhandledKeyEvent(view, event);
		// }
		//
		// @Override
		// public void onScaleChanged(WebView view, float oldScale, float
		// newScale) {
		// // TODO Auto-generated method stub
		// super.onScaleChanged(view, oldScale, newScale);
		// }
		//
		// @Override
		// public void onReceivedLoginRequest(WebView view, String realm, String
		// account, String args) {
		// // TODO Auto-generated method stub
		// super.onReceivedLoginRequest(view, realm, account, args);
		// }

	}
}
