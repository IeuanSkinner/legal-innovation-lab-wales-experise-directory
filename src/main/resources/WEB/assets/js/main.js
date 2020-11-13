var resultSize = 10;

var highlightResult = function(result) {
	var filter = window.filter.val();

	filter.split(' ').forEach(function(term) {
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

var displayResults = function(results) {
	window.tableBody.empty();

	var hits = results.hits.hits;

	// No. of hits less than or equal to number we display therefore button can be hidden OR
	// no. of hits is equal to the number we're displaying therefore we've loaded as many as we can.
	if (hits.length < resultSize || hits.length === window.tableBody.children().length) {
		window.loadMoreBtn.addClass('hidden');
		resultSize = 10; // Reset count for subsequent searches.
	} else {
		window.loadMoreBtn.removeClass('hidden');
	}

	if (hits.length === 0) {
		window.noResults.removeClass('hidden');
	} else {
		window.noResults.addClass('hidden');
	}

	hits.forEach(function (staffMember) {
		var data = staffMember['_source'];
		var name = highlightResult(data.name);
		var expertise = highlightResult(data.expertise.join(', '));

		var tpl = '<tr>' +
			'<td><a href="' + data.url + '">' + name + '</a></td>' +
			'<td>' + expertise + '</td>' +
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
		'query': {
			'bool': {}
		}
	};

	// If a department radiobutton is selected the query results must belong to that department.
	if (department && department !== 'all') {
		query.query.bool['must'] = [{
			'match_phrase': {
				'department': department
			}
		}];
	}

	// If a user has entered some filter terms then the search result should contain at least one of those terms.
	if (filter) {
		query.query.bool['should'] = [];

		filter.split(' ').forEach(function(term) {
			query.query.bool.should.push({
				'wildcard': {
					'name': '*'+term+'*'
				}
			});
			query.query.bool.should.push({
				'wildcard': {
					'expertise': '*'+term+'*'
				}
			});
		});

		query.query.bool['minimum_should_match'] = 1;
	}

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
	window.departments = $( 'input[name="department"]' );

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