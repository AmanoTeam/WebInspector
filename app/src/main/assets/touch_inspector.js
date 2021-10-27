function injectTouchInpector() {
   
   document.addEventListener('click', function(event) {
   	if (!window.canOpenTouchInspector) {
			return;
		}
		
		event.preventDefault();
		event.stopPropagation();
		
		var touchPoint = document.elementFromPoint(event.clientX, event.clientY);
		
		window.setOuterHTML = function(source) {
			touchPoint.outerHTML = source;
		};
		
		window.setParentOuterHTML = function(source) {
			touchPoint.parentElement.outerHTML = source;
		};
		
		webInpectorJavaScriptInterface.showTouchInspector(touchPoint.parentElement.outerHTML, touchPoint.outerHTML);
	
   });
   
   window.isTouchInspectorEnabled = true;
   window.canOpenTouchInspector = false;

}

function toggleTouchInpector() {
	
	window.canOpenTouchInspector = !window.canOpenTouchInspector;
	
	if (window.canOpenTouchInspector) {
		window.canOpenSelectors = false;
	}
	
	setTimeout(function() {
		webInpectorJavaScriptInterface.showTouchInspectorState(window.canOpenTouchInspector);
	}, 100);
	
}

if (!window.isTouchInspectorEnabled) {
   injectTouchInpector();
}
