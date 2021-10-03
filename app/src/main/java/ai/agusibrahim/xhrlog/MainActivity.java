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
					setClipboard((String) adapterView.getItemAtPosition(position));
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
						final String jsExpression = jsConsoleInput.getText().toString().replaceAll(";$","");
						
						webView.loadUrl(String.format("javascript:%s%s%s", jsExpression.startsWith("console.") ? "": "console.log(", jsExpression, jsExpression.startsWith("console.") ? "": ");"));
						
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
		
		mainDrawer.setDrawerListener(new DrawerLayout.DrawerListener() {
				
				@Override
				public void onDrawerSlide(final View drawerView, final float slideOffset) {
					// TODO: Implement this method
				}
				
				@Override
				public void onDrawerOpened(final View drawerView) {
					if (mainDrawer.isDrawerOpen(Gravity.RIGHT))
						setTitle("XHR logs");
					else if (mainDrawer.isDrawerOpen(Gravity.LEFT))
						setTitle("Network logs");
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
		
		// "Web View" stuff
		webView = (WebView) findViewById(R.id.webView);
		
		webView.getSettings().setJavaScriptEnabled(true);
		
		webView.setWebContentsDebuggingEnabled(true);
		
		webView.addJavascriptInterface(new MyJavaScriptInterface(), "$$");
		
		webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					setTitle("Loading...");
					super.onPageStarted(view, url, favicon);
				}
				
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, final String url) {
					// capture semua request ke listview
					view.post(new Runnable() {
							@Override
							public void run() {
								networkLogs.add(url);
								arrayAdapter.notifyDataSetChanged();
							}
						});
					return super.shouldInterceptRequest(view, url);
				}
				
				@Override
				public void onPageFinished(final WebView webView, final String url) {
					setTitle(webView.getTitle());
					
					webView.loadUrl("javascript:function injek3() {window.hasdir = 1;window.dir = function(n) {var r = [];for(var t in n)'function' = = typeof n[t]&&r.push(t);return r}};if (window.hasdir!= 1) {injek3();}");
					webView.loadUrl("javascript:function injek2() {window.touchblock = 0,window.dummy1 = 1,document.addEventListener('click',function(n) {if (1 = = window.touchblock) {n.preventDefault();n.stopPropagation();var t = document.elementFromPoint(n.clientX,n.clientY);window.ganti = function(n) {t.outerHTML = n},window.gantiparent = function(n) {t.parentElement.outerHTML = n},$$.print(t.parentElement.outerHTML, t.outerHTML)}},!0)}1!= window.dummy1&&injek2();");
					webView.loadUrl("javascript:function injek() {window.hasovrde = 1;var e = XMLHttpRequest.prototype.open;XMLHttpRequest.prototype.open = function(ee,nn,aa) {this.addEventListener('load',function() {$$.log(this.responseText, nn, JSON.stringify(arguments))}),e.apply(this,arguments)}};if (window.hasovrde!= 1) {injek();}");
					
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
		urlInputView.setQueryHint("Goto URL");
		
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
						Toast.makeText(getApplicationContext(), "Unrecognized URI or unsupported protocol", Toast.LENGTH_SHORT).show();
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
				webView.loadUrl("javascript:window.touchblock = !window.touchblock;setTimeout(function() {$$.blocktoggle(window.touchblock)}, 100);");
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
		dl.setTitle("Source");
		dl.setView(v);
		dl.setPositiveButton("Save", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface p1, int p2) {
					webView.loadUrl("javascript:window.ganti"+((boolean)ed.getTag()?"parent":"")+"('"+ed.getText().toString()+"');");
				}
			});
		dl.setNeutralButton("Parent", null);
		dl.setNegativeButton("Close", null);
		AlertDialog dlg = dl.show();
		final Button prntBtn = dlg.getButton(AlertDialog.BUTTON_NEUTRAL);
		prntBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View view) {
					ed.setText((boolean)ed.getTag()?r:s);
					prntBtn.setText((boolean)ed.getTag()?"Perent":"Inner");
					ed.setTag(!(boolean)ed.getTag());
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
		public void print(final String contentparent, final String content) {
			webView.post(new Runnable() {
					@Override
					public void run() {
						showSourceDialog(contentparent, content);
					}
				});
		}
		@JavascriptInterface
		@SuppressWarnings("unused")
		public void blocktoggle(final String val) {
			webView.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), val.matches("(1|true)")?"Touch Inspector Activated":"Touch Inspector Deactivated", Toast.LENGTH_SHORT).show();
					}
				});

		}
	}
	
	// fungsi set clipboard utk semua versi API
	private void setClipboard(String text) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(text);
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
			clipboard.setPrimaryClip(clip);
		}
	}
}
