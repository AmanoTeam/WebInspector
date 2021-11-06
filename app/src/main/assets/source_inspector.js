function injectSourceInspector() {
   
   document.addEventListener('click', function(event) {
   	if (!window.canOpenSourceInspector) {
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
		
		webInpectorJavaScriptInterface.showSourceInspector(touchPoint.parentElement.outerHTML, touchPoint.outerHTML);
	
   });
   
   window.isSourceInspectorEnabled = true;
   window.canOpenSourceInspector = false;

}

function toggleSourceInspector() {
	
	window.canOpenSourceInspector = !window.canOpenSourceInspector;
	
	if (window.canOpenSourceInspector) {
		window.canOpenSelectors = false;
	}
	
	setTimeout(function() {
		webInpectorJavaScriptInterface.showSourceInspectorState(window.canOpenSourceInspector);
	}, 100);
	
	return true;
	
}

if (!window.isSourceInspectorEnabled) {
   injectSourceInspector();
}
