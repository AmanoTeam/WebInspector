function injectSourceInspector() {
   
   document.addEventListener('click', function(event) {
   	if (!window.canOpenSourceInspector) {
			return;
		}
		
		event.preventDefault();
		event.stopPropagation();
		
		var touchPoint = document.elementFromPoint(event.clientX, event.clientY);
		
		if (touchPoint.parentElement == null) {
			var parentOuterHTML = null;
		} else {
			var parentOuterHTML = touchPoint.parentElement.outerHTML;
		}
		
		touchPoint.style.border = "1px solid red";
		
		var target = event.target;
		var selector = getSelectorTo(target);
		
		window.setOuterHTML = function(source) {
			touchPoint.outerHTML = source;
		};
		
		window.setParentOuterHTML = function(source) {
			touchPoint.parentElement.outerHTML = source;
		};
		
		webInpectorJavaScriptInterface.showSourceInspector(parentOuterHTML, touchPoint.outerHTML, selector);
	
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
