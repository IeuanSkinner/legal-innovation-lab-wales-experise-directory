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
}

var displayResults = function(results) {
	var tableBody = $('#expertise-list tbody');
	tableBody.empty();

	results.hits.hits.forEach(function( staffMember ) {
		var data = staffMember['_source'];
		var name = highlightResult(data.name);
		var expertise = highlightResult(data.expertise.join(', '));

		var tpl = '<tr>' +
			'<td><a href="' + data.url + '">' + name + '</a></td>' +
			'<td>' + expertise + '</td>' +
		'</tr>';

		tableBody.append(tpl);
	});
}

var search = function() {
	var filter = window.filter.val();
	var department = $( 'input[name="department"]:checked ').val();
	var query = {
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

	if (filter) {
		query.query.bool['should'] = [];

		filter.split(' ').forEach(function(term) {
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

	// Search on page load.
	search();

	// Search when department has changed.
	$( 'input[name="department"]' ).change(search);

	// Search after uses types.
	window.filter.on('keyup', function() {
		setTimeout(search, 500);
	});
});