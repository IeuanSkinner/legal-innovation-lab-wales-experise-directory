var resultSize = 10;

var highlightResult = function(result, queryTerms) {
	queryTerms.forEach(function(term) {
		var lowerCaseResult = result.toLowerCase();
		var lowerCaseTerm = term.toLowerCase();
		var startIndex = lowerCaseResult.indexOf(lowerCaseTerm);

		if (startIndex > -1) {
			var endIndex = startIndex + term.length;
			result = result.slice(0, startIndex) +
				'<span class="highlight">' + result.slice(startIndex, endIndex) + '</span>' + result.slice(endIndex);
		}
	});

	return result;
};

var displayResults = function(data) {
	window.tableBody.empty();

	var results = data.results;
	var queryTerms = data.queryTerms;

	// No. of hits less than or equal to number we display therefore button can be hidden OR
	// no. of hits is equal to the number we're displaying therefore we've loaded as many as we can.
	if (results.length < resultSize || results.length === window.tableBody.children().length) {
		window.loadMoreBtn.addClass('hidden');
		resultSize = 10; // Reset count for subsequent searches.
	} else {
		window.loadMoreBtn.removeClass('hidden');
	}

	if (results.length === 0) {
		window.noResults.removeClass('hidden');
	} else {
		window.noResults.addClass('hidden');
	}

	results.forEach(function (staffMember) {
		var data = staffMember['_source'];
		var name = highlightResult(data.name, queryTerms);

		for (var i = 0; i < data.expertise.length; i++) {
			data.expertise[i] = highlightResult(data.expertise[i], queryTerms);
		}

		var tpl = '<tr>' +
			'<td><a href="' + data.url + '">' + name + '</a></td>' +
			'<td>' + data.expertise.join(document.body.offsetWidth <= 768 ? '<br>' : ', ') + '</td>' +
			'</tr>';

		window.tableBody.append(tpl);
	});
};

var search = function() {
	var filter = window.filter.val();
	var department = window.departments.val();
	var query = {
		'from': 0,
		'size': resultSize,
		'department': department,
		'filterTerms': filter
	};

	$.ajax({
		type: "POST",
		url: '/search',
		dataType: 'json',
		contentType: 'application/json',
		data: JSON.stringify(query),
		success: displayResults,
		fail: function() {
			alert('Unable to get search results!');
		}
	});
};

$( document ).ready(function() {
	window.filter = $( '.filter input' );
	window.tableBody = $( '#expertise-list tbody' );
	window.loadMoreBtn = $( '.content button' );
	window.noResults = $( '.no-results' );
	window.departments = $( 'select[name="departments"]' );

	// Search on page load.
	search();

	// Search when department has changed.
	window.departments.change(search);

	// Search after uses types.
	window.filter.on('keyup', function() {
		setTimeout(search, 500);
	});

	window.loadMoreBtn.on('click', function() {
		resultSize += 10;
		search();
	});
});