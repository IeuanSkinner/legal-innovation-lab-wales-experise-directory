let resultSize = 10;

const highlightResult = (result, queryTerms) => {
	queryTerms.forEach(term => {
		const lowerCaseResult = result.toLowerCase();
		const lowerCaseTerm = term.toLowerCase();
		const startIndex = lowerCaseResult.indexOf(lowerCaseTerm);

		if (startIndex > -1) {
			const endIndex = startIndex + term.length;
			const beginning = result.slice(0, startIndex);
			const highlight = result.slice(startIndex, endIndex);
			const end = result.slice(endIndex);

			result = `${beginning}<span class="highlight">${highlight}</span>${end}`;
		}
	});

	return result;
};

const displayResults = data => {
	window.tableBody.empty();

	const results = data['results'];
	const queryTerms = data['query_terms'];

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

	results.forEach(staffMember => {
		const data = staffMember['_source'];
		const name = highlightResult(data['name'], queryTerms);

		for (let i = 0; i < data['expertise'].length; i++) {
			data['expertise'][i] = highlightResult(data['expertise'][i], queryTerms);
		}

		const expertise = data['expertise'].join(document.body.offsetWidth <= 768 ? '<br>' : ', ');
		const row = `<tr><td><a href="${data['url']}">${name}</a></td><td>${expertise}</td></tr>`;

		window.tableBody.append(row);
	});
};

const search = () => {
	$.ajax({
		type: 'POST',
		url: '/search',
		dataType: 'json',
		contentType: 'application/json',
		data: JSON.stringify({
			'from': 0,
			'size': resultSize,
			'department': window.departments.val(),
			'filter_terms': window.filter.val()
		}),
		success: displayResults,
		fail: () => {
			alert('Unable to get search results!');
		}
	});
};

$( document ).ready(() => {
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
	window.filter.on('keyup', _.debounce(search, 300));

	window.loadMoreBtn.on('click', () => {
		resultSize += 10;
		search();
	});
});