package com.amanoteam.webinspector;

import java.util.ArrayList;
import java.util.List;

import android.app.UiModeManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import com.google.android.material.navigation.NavigationView;

import com.amanoteam.webinspector.R;
import com.amanoteam.webinspector.SettingsActivity;

public class MainActivity extends AppCompatActivity {
	
	private AppCompatTextView jsConsoleLogs;
	
	private NavigationView jsNavigationView;
	private NavigationView networkLogsNavigationView;
	
	private List<String> networkLogs = new ArrayList<String>();
	
	private WebView webView;
	private WebSettings webSettings;
	
	private ArrayAdapter<String> arrayAdapter;
	
	private SearchView urlInputView;
	
	private DrawerLayout mainDrawer;
	
	private MenuItem clearLogsButton;
	private MenuItem touchInspectorItem;
	private MenuItem jsConsoleItem;
	
	private View homeScreenView;
	
	private boolean isDarkMode = false;
	private boolean textInputStyleIsRoundCorners = false;
	
	private final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(final SharedPreferences settings, final String key) {
			if (key.equals("appTheme") || key.equals("textInputStyle")) {
				recreate();
			} else if (key.equals("enableJavascript")) {
				final boolean enableJavascript = settings.getBoolean("enableJavascript", true);
				webSettings.setJavaScriptEnabled(enableJavascript);
				
				if (enableJavascript) {
					jsConsoleItem.setEnabled(true);
					touchInspectorItem.setEnabled(true);
				} else {
					jsConsoleItem.setEnabled(false);
					touchInspectorItem.setEnabled(false);
				}
			} else if (key.equals("allowOpeningWindowsAutomatically")) {
				final boolean allowOpeningWindowsAutomatically = settings.getBoolean("allowOpeningWindowsAutomatically", false);
				webSettings.setJavaScriptCanOpenWindowsAutomatically(allowOpeningWindowsAutomatically);
			} else if (key.equals("blockNetworkImage")) {
				final boolean blockNetworkImage = settings.getBoolean("blockNetworkImage", false);
				webSettings.setBlockNetworkImage(blockNetworkImage);
			} else if (key.equals("blockNetworkLoads")) {
				final boolean blockNetworkLoads = settings.getBoolean("blockNetworkLoads", false);
				webSettings.setBlockNetworkLoads(blockNetworkLoads);
			} else if (key.equals("cacheMode")) {
				final String cacheMode = settings.getString("cacheMode", "LOAD_DEFAULT");
				
				if (cacheMode.equals("LOAD_CACHE_ELSE_NETWORK")) {
					webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
				} else if (cacheMode.equals("LOAD_NO_CACHE")) {
					webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
				} else if (cacheMode.equals("LOAD_CACHE_ONLY")) {
					webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
				} else {
					webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
				}
			} else if (key.equals("userAgent")) {
				final String userAgent = settings.getString("userAgent", "default");
				
				if (userAgent.equals("custom")) {
					final String customUserAgent = settings.getString("customUserAgent", "");
					webSettings.setUserAgentString(customUserAgent);
				} else if (userAgent.equals("default")) {
					webSettings.setUserAgentString(null);
				} else {
					webSettings.setUserAgentString(userAgent);
				}
			}
				
			
		}
	};
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		
		// Preferences stuff
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
		
		// Dark mode stuff
		final String appTheme = settings.getString("appTheme", "follow_system");
		
		if (appTheme.equals("follow_system")) {
			// Snippet from https://github.com/Andrew67/dark-mode-toggle/blob/11c1e16071b301071be0c4715a15fcb031d0bb64/app/src/main/java/com/andrew67/darkmode/UiModeManagerUtil.java#L17
			final UiModeManager uiModeManager = ContextCompat.getSystemService(this, UiModeManager.class);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
				isDarkMode = true;
			}
		} else if (appTheme.equals("dark")) {
			isDarkMode = true;
		}
		
		// Text input style stuff
		final String textInputStyle = settings.getString("textInputStyle", "square");
		
		if (textInputStyle.equals("round")) {
			textInputStyleIsRoundCorners = true;
		}
		
		if (isDarkMode) {
			setTheme(R.style.DarkTheme);
		}
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_activity);
		
		// ListView and EditText stuff
		final ListView networkLogsListView = (ListView) findViewById(R.id.network_logs_list);
		final AppCompatEditText jsConsoleInput = (AppCompatEditText) findViewById(R.id.javascript_console_input);
		
		// This needs to come after setContentView() (see https://stackoverflow.com/a/43635618)
		if (isDarkMode) {
			if (textInputStyleIsRoundCorners) {
				networkLogsListView.setBackgroundResource(R.drawable.round_dark_background);
				jsConsoleInput.setBackgroundResource(R.drawable.round_dark_background);
			} else {
				networkLogsListView.setBackgroundResource(R.drawable.square_dark_background);
				jsConsoleInput.setBackgroundResource(R.drawable.square_dark_background);
			}
		} else {
			if (textInputStyleIsRoundCorners) {
				networkLogsListView.setBackgroundResource(R.drawable.round_light_background);
				jsConsoleInput.setBackgroundResource(R.drawable.round_light_background);
			}
		}
		
		// Action bar stuff
		final Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(mainToolbar);
		
		// "Network logs" stuff
		networkLogsListView.setAdapter(arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, networkLogs));
		
		networkLogsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
					final String urlToCopy = (String) adapterView.getItemAtPosition(position);
					final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			        clipboard.setPrimaryClip(ClipData.newPlainText("URL", urlToCopy));
					
					Toast.makeText(getApplicationContext(), R.string.copied_to_clipboard_toast, Toast.LENGTH_SHORT).show();
					
					adapterView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
					return true;
				}
			}
		);
		
		networkLogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
					webView.loadUrl((String) adapterView.getItemAtPosition(position));
				}
			}
		);
		
		networkLogsNavigationView = (NavigationView) findViewById(R.id.network_logs_navigation);
		
		networkLogsNavigationView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
				@Override
				public WindowInsets onApplyWindowInsets(final View view, final WindowInsets windowInsets) {
					return windowInsets;
				}
			}
		);
		
		// "JavaScript console" stuff
		jsConsoleInput.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(final View view, int keyCode, final KeyEvent keyEvent) {
					if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						
						final String jsExpression = jsConsoleInput.getText().toString();
						
						if (jsExpression.startsWith("console.")) {
							webView.evaluateJavascript(jsExpression, null);
						} else {
							final String modifiedJsExpression = String.format("%s%s%s", "console.log(", jsExpression.replaceAll(";*$",""), ");");
							webView.evaluateJavascript(modifiedJsExpression, null);
						}
						
						jsConsoleInput.setText("");
						
						return true;
					}
					return false;
				}
			}
		);
		
		// "JavaScript console" stuff
		jsConsoleLogs = (AppCompatTextView) findViewById(R.id.javascript_logs_list);
		
		jsNavigationView = (NavigationView) findViewById(R.id.javascript_logs_navigation);
		
		jsNavigationView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
				@Override
				public WindowInsets onApplyWindowInsets(final View view, final WindowInsets windowInsets) {
					return windowInsets;
				}
			}
		);
		
		// Some layout views stuff
		homeScreenView = findViewById(R.id.home_screen);
		
		// Drawer stuff
		mainDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		
		mainDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
				
				@Override
				public void onDrawerSlide(final View drawerView, final float slideOffset) {
					// TODO: Implement this method
				}
				
				@Override
				public void onDrawerOpened(final View drawerView) {
					if (mainDrawer.isDrawerOpen(Gravity.RIGHT))
						setTitle(R.string.javascript_logs);
					else if (mainDrawer.isDrawerOpen(Gravity.LEFT))
						setTitle(R.string.network_logs);
					clearLogsButton.setVisible(true);
				}
				
				@Override
				public void onDrawerClosed(final View view) {
					setTitle(webView.getTitle().length() > 1 ? webView.getTitle(): getResources().getString(R.string.app_name));
					clearLogsButton.setVisible(false);
				}
				
				@Override
				public void onDrawerStateChanged(final int newState) {
					// TODO: Implement this method
				}
				
			}
		);
		
		// Web View stuff
		webView = (WebView) findViewById(R.id.web_view);
		webSettings = webView.getSettings();
		
		final boolean enableJavascript = settings.getBoolean("enableJavascript", true);
		webSettings.setJavaScriptEnabled(enableJavascript);
		
		final boolean allowOpeningWindowsAutomatically = settings.getBoolean("allowOpeningWindowsAutomatically", false);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(allowOpeningWindowsAutomatically);
		
		final boolean blockNetworkImage = settings.getBoolean("blockNetworkImage", false);
		webSettings.setBlockNetworkImage(blockNetworkImage);
		
		final boolean blockNetworkLoads = settings.getBoolean("blockNetworkLoads", false);
		webSettings.setBlockNetworkLoads(blockNetworkLoads);
		
		final String cacheMode = settings.getString("cacheMode", "LOAD_DEFAULT");
		
		if (cacheMode.equals("LOAD_CACHE_ELSE_NETWORK")) {
			webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		} else if (cacheMode.equals("LOAD_NO_CACHE")) {
			webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		} else if (cacheMode.equals("LOAD_CACHE_ONLY")) {
			webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
		} else {
			webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		}
		
		final String userAgent = settings.getString("userAgent", "default");
		
		if (userAgent.equals("custom")) {
			final String customUserAgent = settings.getString("customUserAgent", "");
			webSettings.setUserAgentString(customUserAgent);
		} else if (userAgent.equals("default")) {
			webSettings.setUserAgentString(null);
		} else {
			webSettings.setUserAgentString(userAgent);
		}
		
		webView.setWebContentsDebuggingEnabled(true);
		
		webView.addJavascriptInterface(new MyJavaScriptInterface(), "jsInterface");
		
		webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					setTitle(R.string.page_loading);
					super.onPageStarted(view, url, favicon);
				}
				
				@Override
				public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
					final String requestUrl = request.getUrl().toString();
					
					if (requestUrl.startsWith("http:") || requestUrl.startsWith("https:")) {
						return false;
					}
					
					final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestUrl));
					final Intent intentChooser = Intent.createChooser(intent, getString(R.string.intent_chooser_open_with));
					
					startActivity(intentChooser);
					
					return true;
				}
				
				@Override
				public WebResourceResponse shouldInterceptRequest(final WebView view, final WebResourceRequest request) {
					
					final String requestUrl = request.getUrl().toString();
					
					view.post(new Runnable() {
							@Override
							public void run() {
								networkLogs.add(requestUrl);
								arrayAdapter.notifyDataSetChanged();
							}
						});
					return super.shouldInterceptRequest(view, request);
				}
				
				@Override
				public void onPageFinished(final WebView webView, final String url) {
					setTitle(webView.getTitle());
					
					webView.evaluateJavascript("function injek3(){window.hasdir=1;window.dir=function(n){var r=[];for(var t in n)'function'==typeof n[t]&&r.push(t);return r}};if(window.hasdir!=1){injek3();}", null);
					webView.evaluateJavascript("function injek2(){window.touchblock=0,window.dummy1=1,document.addEventListener('click',function(n){if(1==window.touchblock){n.preventDefault();n.stopPropagation();var t=document.elementFromPoint(n.clientX,n.clientY);window.ganti=function(n){t.outerHTML=n},window.gantiparent=function(n){t.parentElement.outerHTML=n},jsInterface.print(t.parentElement.outerHTML, t.outerHTML)}},!0)}1!=window.dummy1&&injek2();", null);
					webView.evaluateJavascript("function injek(){window.hasovrde=1;var e=XMLHttpRequest.prototype.open;XMLHttpRequest.prototype.open=function(ee,nn,aa){this.addEventListener('load',function(){jsInterface.log(this.responseText, nn, JSON.stringify(arguments))}),e.apply(this,arguments)}};if(window.hasovrde!=1){injek();}", null);
					
					super.onPageFinished(webView, url);
				}
				
			}
		);
		
		webView.setWebChromeClient(new WebChromeClient() {
				@Override
				public boolean onConsoleMessage(final ConsoleMessage consoleMessage) {
					final String text = consoleMessage.message();
					final String separator = "\n--------------------\n";
					
					final String consoleMessageText = String.format("%s%s", text, separator);
					
					jsConsoleLogs.append(consoleMessageText);
					
					return false;
				}
			}
		);
	
	}

	@Override
	public void onBackPressed() {
		// browser bisa di back, lakukan back. jika tidak maka lakukan back pada aplikasi (keluar)
		if (webView.canGoBack()) {
			webView.goBack();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		
		final MenuItem urlInput = menu.findItem(R.id.goto_url);
		
		// "Clear logs" button
		clearLogsButton = menu.findItem(R.id.menu_clear);
		clearLogsButton.setVisible(false);
		
		// "Touch inspector" item
		touchInspectorItem = menu.findItem(R.id.menu_touchinspect);
		
		// "JavaScript console" item
		jsConsoleItem = menu.findItem(R.id.menu_jslog);
		
		if (!webSettings.getJavaScriptEnabled()) {
			jsConsoleItem.setEnabled(false);
			touchInspectorItem.setEnabled(false);
		}
		
		// URL input
		urlInputView = (SearchView) urlInput.getActionView();
		urlInputView.setQueryHint(getString(R.string.url_input_hint));
		
		urlInputView.setOnSearchClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View view) {
					urlInputView.setQuery(webView.getUrl(), false);
				}
			}
		);
		
		urlInputView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(final String url) {
					
					if (url.matches("^[a-z]+:.+")) {
						webView.loadUrl(url);
					} else {
						webView.loadUrl(String.format("http://%s", url));
					}
					
					urlInput.collapseActionView();
						
					homeScreenView.setVisibility(View.GONE);
					webView.setVisibility(View.VISIBLE);
					
					return false;
				}
				
				@Override
				public boolean onQueryTextChange(String p1) {
					return false;
				}
				
			}
		);
		
		return super.onCreateOptionsMenu(menu);
	}

	// menu click handler
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_touchinspect:
				webView.evaluateJavascript("window.touchblock = !window.touchblock;setTimeout(function() {jsInterface.blocktoggle(window.touchblock)}, 100);", null);
				return true;
			case R.id.menu_jslog:
				mainDrawer.closeDrawer(Gravity.LEFT);
				mainDrawer.openDrawer(Gravity.RIGHT);
				return true;
			case R.id.menu_netlog:
				mainDrawer.closeDrawer(Gravity.RIGHT);
				mainDrawer.openDrawer(Gravity.LEFT);
				return true;
			case R.id.menu_settings:
				final Intent settingsIntent = new Intent(this, SettingsActivity.class);
				startActivity(settingsIntent);
				return true;
			case R.id.menu_exit:
				finish();
				return true;
			case R.id.menu_clear:
				if (mainDrawer.isDrawerOpen(Gravity.RIGHT)) {
					// JavaScript console logs
					jsConsoleLogs.setText("");
				} else if (mainDrawer.isDrawerOpen(Gravity.LEFT)) {
					// Network logs
					networkLogs.clear();
				}
				arrayAdapter.notifyDataSetChanged();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	// js interface
	class MyJavaScriptInterface {
		@JavascriptInterface
		@SuppressWarnings("unused")
		public void log(final String content, final String url, final String arg) {
			webView.post(new Runnable() {
					@Override
					public void run() {
						jsConsoleLogs.append(String.format("REQ: %s\nARG: %s\nRESP: %s\n--------------------\n",url,arg, content));
					}
				});
		}
		@JavascriptInterface
		@SuppressWarnings("unused")
		public void print(final String contentParent, final String content) {
			webView.post(new Runnable() {
					@Override
					public void run() {
						final LayoutInflater layoutInflater = getLayoutInflater();
						final View sourceView = layoutInflater.inflate(R.layout.source_code_viewer, null);
						
						final AppCompatEditText sourceCodeEditor = (AppCompatEditText) sourceView.findViewById(R.id.source_code_editor_text);
						
						if (isDarkMode) {
							if (textInputStyleIsRoundCorners) {
								sourceCodeEditor.setBackgroundResource(R.drawable.round_dark_background);
							} else {
								sourceCodeEditor.setBackgroundResource(R.drawable.square_dark_background);
							}
						} else {
							if (textInputStyleIsRoundCorners) {
								sourceCodeEditor.setBackgroundResource(R.drawable.round_light_background);
							}
						}
						
						sourceCodeEditor.setText(content);
						sourceCodeEditor.setTag(false);
						
						final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
						alertDialogBuilder.setTitle(R.string.touch_inspector_title);
						alertDialogBuilder.setView(sourceView);
						alertDialogBuilder.setPositiveButton(R.string.touch_inspector_save_button, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface p1, int p2) {
									webView.loadUrl("javascript:window.ganti" + ((boolean) sourceCodeEditor.getTag() ? "parent": "") + "('" + sourceCodeEditor.getText().toString() + "');", null);
								}
							}
						);
						alertDialogBuilder.setNeutralButton(R.string.touch_inspector_parent_button, null);
						alertDialogBuilder.setNegativeButton(R.string.touch_inspector_close_button, null);
						
						final AlertDialog alertDialog = alertDialogBuilder.show();
						
						final Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
						neutralButton.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(final View view) {
									sourceCodeEditor.setText((boolean) sourceCodeEditor.getTag() ? content: contentParent);
									neutralButton.setText((boolean) sourceCodeEditor.getTag() ? R.string.touch_inspector_parent_button: R.string.touch_inspector_inner_button);
									sourceCodeEditor.setTag(!(boolean) sourceCodeEditor.getTag());
								}
							}
						);
					}
				}
			);
		}
		@JavascriptInterface
		@SuppressWarnings("unused")
		public void blocktoggle(final String value) {
			webView.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), value.matches("(1|true)") ? R.string.touch_inspector_enabled_toast: R.string.touch_inspector_disabled_toast, Toast.LENGTH_SHORT).show();
					}
				});

		}
	}
	
}
