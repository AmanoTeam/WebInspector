package ai.agusibrahim.xhrlog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.view.*;
import com.google.android.material.navigation.NavigationView;
import android.os.Build;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.*;
import android.webkit.WebResourceRequest;
import android.webkit.JavascriptInterface;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.AppCompatEditText;
import android.view.inputmethod.EditorInfo;
import java.util.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.webkit.WebResourceResponse;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;
import android.view.MenuItem;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.AdapterView;
import android.widget.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import android.webkit.WebChromeClient;
import java.util.concurrent.*;
import android.content.ClipData;
import android.content.ClipboardManager;

import ai.agusibrahim.xhrlog.R;

public class MainActivity extends AppCompatActivity {
	
	private AppCompatTextView xhrLogs;
	
	private NavigationView xhrNavigationView;
	private NavigationView networkLogsNavigationView;
	
	private List<String> networkLogs = new ArrayList<String>();
	
	private WebView webView;
	
	private ArrayAdapter<String> arrayAdapter;
	
	private SearchView urlInputView;
	
	private DrawerLayout mainDrawer;
	
	private MenuItem clearLogsButton;
	
	private View homeScreenView;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		// Action bar stuff
		final Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(mainToolbar);
		
		// "Network logs" stuff
		final ListView networkLogsListView = (ListView) findViewById(R.id.networkLogsListView);
		networkLogsListView.setAdapter(arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, networkLogs));
		
		networkLogsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
					final String urlToCopy = (String) adapterView.getItemAtPosition(position);
					final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			        clipboard.setPrimaryClip(ClipData.newPlainText("URL", urlToCopy));
					
					Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
					
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
		
		networkLogsNavigationView = (NavigationView) findViewById(R.id.networkLogsNavigationView);
		
		networkLogsNavigationView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
				@Override
				public WindowInsets onApplyWindowInsets(final View view, final WindowInsets windowInsets) {
					return windowInsets;
				}
			}
		);
		
		// "JavaScript console" stuff
		final AppCompatEditText jsConsoleInput = (AppCompatEditText) findViewById(R.id.jsConsoleInput);
		
		jsConsoleInput.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(final View view, int keyCode, final KeyEvent keyEvent) {
					if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						
						webView.evaluateJavascript(jsConsoleInput.getText().toString(), null);
						
						jsConsoleInput.setText("");
						return true;
					}
					return false;
				}
			}
		);
		
		// "XML HTTP Request logs" stuff
		xhrLogs = (AppCompatTextView) findViewById(R.id.xhrLogs);
		
		xhrNavigationView = (NavigationView) findViewById(R.id.xhrNavigationView);
		
		xhrNavigationView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
				@Override
				public WindowInsets onApplyWindowInsets(final View view, final WindowInsets windowInsets) {
					return windowInsets;
				}
			}
		);
		
		// Some layout views stuff
		homeScreenView = findViewById(R.id.homeScreen);
		
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
						setTitle(R.string.xhr_logs);
					else if (mainDrawer.isDrawerOpen(Gravity.LEFT))
						setTitle(R.string.network_logs);
					clearLogsButton.setVisible(true);
				}
				
				@Override
				public void onDrawerClosed(final View view) {
					setTitle(webView.getTitle().length()>1?webView.getTitle():getResources().getString(R.string.app_name));
					clearLogsButton.setVisible(false);
				}
				
				@Override
				public void onDrawerStateChanged(final int newState) {
					// TODO: Implement this method
				}
				
			}
		);
		
		// Web View stuff
		webView = (WebView) findViewById(R.id.webView);
		
		webView.getSettings().setJavaScriptEnabled(true);
		
		webView.setWebContentsDebuggingEnabled(true);
		
		webView.addJavascriptInterface(new MyJavaScriptInterface(), "jsInterface");
		
		webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					setTitle(R.string.page_loading);
					super.onPageStarted(view, url, favicon);
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
				public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
					xhrLogs.append(consoleMessage.message()+"\n--------------------\n");
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
		
		// "Go to web address" button
		urlInputView = (SearchView) urlInput.getActionView();
		urlInputView.setQueryHint(getString(R.string.goto_url_hint));
		
		urlInputView.setOnSearchClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View view) {
					urlInputView.setQuery(webView.getUrl(), false);
				}
			}
		);
		
		urlInputView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(final String urlTextInput) {
					
					if (urlTextInput.startsWith("http://") || urlTextInput.startsWith("https://")) {
						webView.loadUrl(urlTextInput);
						
						urlInput.collapseActionView();
						
						homeScreenView.setVisibility(View.GONE);
						webView.setVisibility(View.VISIBLE);
					} else {
						Toast.makeText(getApplicationContext(), R.string.unrecognized_uri_toast, Toast.LENGTH_SHORT).show();
					}
					
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
			case R.id.menu_xhrlog:
				mainDrawer.closeDrawer(Gravity.LEFT);
				mainDrawer.openDrawer(Gravity.RIGHT);
				return true;
			case R.id.menu_netlog:
				mainDrawer.closeDrawer(Gravity.RIGHT);
				mainDrawer.openDrawer(Gravity.LEFT);
				return true;
			case R.id.menu_touchinspect:
				// saat menu Touch Inscpector di klik, maka inject js yang sudah diatur
				webView.evaluateJavascript("window.touchblock = !window.touchblock;setTimeout(function() {jsInterface.blocktoggle(window.touchblock)}, 100);", null);
				return true;
			case R.id.menu_exit:
				finish();
				return true;
			case R.id.menu_clear:
				if (mainDrawer.isDrawerOpen(Gravity.RIGHT)) {
					// XHR logs
					xhrLogs.setText("");
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
	// dialog source code view/edit
	private void showSourceDialog(final String s, final String r) {
		View v = getLayoutInflater().inflate(R.layout.source, null);
		final AppCompatEditText ed = (AppCompatEditText)v.findViewById(R.id.sourceEditText1);
		ed.setText(r);
		ed.setTag(false);
		AlertDialog.Builder dl = new AlertDialog.Builder(this);
		dl.setTitle(R.string.touch_inspector_title);
		dl.setView(v);
		dl.setPositiveButton(R.string.touch_inspector_save_button, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface p1, int p2) {
					webView.evaluateJavascript("window.ganti" + ((boolean) ed.getTag() ? "parent": "") + "('" + ed.getText().toString() + "');", null);
				}
			});
		dl.setNeutralButton(R.string.touch_inspector_parent_button, null);
		dl.setNegativeButton(R.string.touch_inspector_close_button, null);
		AlertDialog dlg = dl.show();
		final Button prntBtn = dlg.getButton(AlertDialog.BUTTON_NEUTRAL);
		prntBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View view) {
					ed.setText((boolean) ed.getTag() ? r: s);
					prntBtn.setText((boolean) ed.getTag() ? R.string.touch_inspector_parent_button: R.string.touch_inspector_inner_button);
					ed.setTag(!(boolean) ed.getTag());
				}
			});
	}
	
	// js interface
	class MyJavaScriptInterface {
		@JavascriptInterface
		@SuppressWarnings("unused")
		public void log(final String content, final String url, final String arg) {
			webView.post(new Runnable() {
					@Override
					public void run() {
						xhrLogs.append(String.format("REQ: %s\nARG: %s\nRESP: %s\n--------------------\n",url,arg, content));
					}
				});
		}
		@JavascriptInterface
		@SuppressWarnings("unused")
		public void print(final String contentParent, final String content) {
			webView.post(new Runnable() {
					@Override
					public void run() {
						showSourceDialog(contentParent, content);
					}
				});
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
