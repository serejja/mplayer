var userid = 0;
var shuffle = false;
var repeat = false;

var tracks = [];
var currentId = 0;
var currentGenre = "";
var currentArtist = "";
var currentCountry = "";
var currentAlbum = "";
var currentYear = 0;
var currentTrack = "";
var currentDuration = "";
var currentTimestamp = 0;

var currentTime = 0;

var DELAY = 200,
clicks = 0,
timer = null;

var onTimeUpdateFunction = function(event) {
	onTimeUpdate();
};

var onTrackEndedFunction = function(event) {
    console.log("song ended");
    scrobble(currentArtist, currentTrack, currentTimestamp);
    playNext();
};

function enableSearching() {
    var searchButton = $('#searchbutton');
    searchButton.click(function(event) {
        var searchText = $('#searchtext').val();
        searchArtists(searchText);
    });
}

function updatePlayer(id){
    var player = $("#jplayer");

    player.jPlayer({
    ready: function () { 
        $(this).jPlayer("setMedia", { 
            mp3: "get?id=" + id
        }); 
        $(this).jPlayer("play");
    },
    swfPath: "/js",
    supplied: "mp3",
    }); 
    player.jPlayer('clearMedia');
    player.jPlayer("setMedia", { 
        mp3: "get?id=" + id
    }); 
    player.jPlayer("play");
    player.bind($.jPlayer.event.ended, onTrackEndedFunction);
    player.bind($.jPlayer.event.timeupdate, onTimeUpdateFunction);

    updateTrackInfo(id);
}

function onTimeUpdate() {
	var time = $('#jplayer').data('jPlayer').status.currentTime;
    var duration = $('#jplayer').data('jPlayer').status.duration;
    $('#now_time').text(parseInt(time/60) + ":" + withLeadingZeros(parseInt(time % 60), 2));
    $('#seek').slider('value', parseInt((time/duration) * 100));
}

function updateTrackInfo(id) {
    $.getJSON("/trackinfo?id=" + id, function(json) {
        var nowplaying = $("#nowplaying");
        var totalTime = $('#total_time');
        currentId = id;
        currentGenre = json.album.artist.genre.name;
        currentArtist = json.album.artist.name;
        if (json.album.artist.country != undefined) {
        	currentCountry = json.album.artist.country.name;
    	}
        currentAlbum = json.album.name;
        currentYear = json.album.year;
        currentTrack = json.name;
        currentDuration = json.duration;
        currentTimestamp = Math.round(+new Date()/1000);
        nowplaying.text(currentArtist + " - " + currentTrack + " (" + currentDuration + ")");
        totalTime.text(currentDuration);
        updateNowPlaying(currentArtist, currentTrack);
        return false;
    });
}

function updateNowPlaying(artist, track) {
	$("#currentartist").text(artist);
    $.getJSON("/updatenowplaying?artist=" + artist + "&track=" + track + "&userid=" + userid, function(json) {
        return false;
    });
}

function scrobble(artist, track, timestamp) {
	$.getJSON("/scrobble?artist=" + artist + "&track=" + track + "&timestamp=" + timestamp + "&userid=" + userid, function(json) {
        return false;
    });	
}

function fillGenres() {
    $.getJSON("/genres", function(json) {
        var table = $("#genrestable");
        table.empty();
        highlightOnClick(table);
        $.each(json, function() {
            var tbl_row = $("<tr>", {class: "loadartists", href: "#artists", id: this.id});
            var tbl_cell = $("<td>", {id: this.id, html: this.name});
            tbl_row.append(tbl_cell);
            var id = this.id;
            setClickHandlers(tbl_row, function() {
                fillArtists("/artists?genreid=" + id);
            }, function() {
                $.getJSON("/tracks?genreid=" + id, function(json) {
                    console.log("/tracks?genreid=" + id);
                    fillTracks(json);
                });
            });
            table.append(tbl_row);
        })
        return false;
    });
}

function fillCountries() {
    $.getJSON("/countries", function(json) {
        var table = $("#countriestable");
        table.empty();
        highlightOnClick(table);
        $.each(json, function() {
            var tbl_row = $("<tr>", {class: "loadartists", href: "#artists", id: this.id});
            var tbl_cell = $("<td>", {id: this.id, html: this.name});
            tbl_row.append(tbl_cell);
            var id = this.id;
            setClickHandlers(tbl_row, function() {
                fillArtists("/artists?countryid=" + id);
            }, function() {
                $.getJSON("/tracks?countryid=" + id, function(json) {
                    console.log("/tracks?countryid=" + id);
                    fillTracks(json);
                });
            });
            table.append(tbl_row);
        })
        return false;
    });
}

function fillArtists(url) {
    console.log(url);
    $.getJSON(url, function(json) {
        var table = $("#artiststable");
        table.empty();
        headers(table, ["Name"]);
        highlightOnClick(table);
        $.each(json, function() {
            var tbl_row = $("<tr>", {href: "#", class: "loadalbums", id: this.id});
            var tbl_cell = $("<td>", {id: this.id, html: this.name});
            tbl_row.append(tbl_cell);
            var id = this.id;
            setClickHandlers(tbl_row, function() {
                fillAlbums(id);
            }, function() {
                $.getJSON("/tracks?artistid=" + id, function(json) {
                    console.log("/tracks?artistid=" + id);
                    fillTracks(json);
                });
            });
            table.append(tbl_row);
        })
        $('#tabs a[href="#artists"]').tab('show');
        return false;
    });
}

function fillAlbums(artistid) {
    console.log("/albums?artistid=" + artistid);
    $.getJSON("/albums?artistid=" + artistid, function(json) {
        var table = $("#albumstable");
        table.empty();
        headers(table, ["Year", "Name", "Format"]);
        highlightOnClick(table);
        $.each(json, function() {
            var tbl_row = $("<tr>", {href: "#", class: "loadtracks", id: this.id});
            var tbl_cell_year = $("<td>", {id: this.id, html: this.year});
            var tbl_cell_name = $("<td>", {id: this.id, html: this.name});
            var tbl_cell_format = $("<td>", {id: this.id, html: this.format});
            tbl_row.append(tbl_cell_year);
            tbl_row.append(tbl_cell_name);
            tbl_row.append(tbl_cell_format);
            var id = this.id;
            setClickHandlers(tbl_row, function() {
                $.getJSON("/tracks?albumid=" + id, function(json) {
                    console.log("/tracks?albumid=" + id);
                    fillTracks(json);
                });
            }, function() {
                alert("Double click");
            });
            table.append(tbl_row);
        })
        $('#tabs a[href="#albums"]').tab('show');
        return false;
    });
}

function fillTracks(json) {
    var table = $("#trackstable");
    table.empty();
    headers(table, [" ", "#", "Title", "Duration", "Artist", "Country", "Album", "Year", "Genre"]);
    highlightOnClick(table);
    var count = 1;

    $.each(json, function() {
        tracks.push(this.id);
        var tbl_row = $("<tr>", {href: "#", id: this.id});
        var play_button = $('<a>', {id: this.id, href: '#', class: 'btn btn-success btn-xs play', html: '&#9658;'});
        var tbl_cell_play = $("<td>", {html: play_button});
        var tbl_cell_no = $("<td>", {html: count++});
        var tbl_cell_name = $("<td>", {html: this.name});
        var tbl_cell_duration = $("<td>", {html: this.duration});
        var tbl_cell_artist = $("<td>", {html: this.album.artist.name});
        var country = this.album.artist.country;
        if (country != undefined) {
            var tbl_cell_country = $("<td>", {html: this.album.artist.country.name});
        } else {
            var tbl_cell_country = $("<td>", {html: "-"});
        }
        var tbl_cell_album = $("<td>", {html: this.album.name});
        var tbl_cell_year = $("<td>", {html: this.album.year});
        var tbl_cell_genre = $("<td>", {html: this.album.artist.genre.name});
        tbl_row.append(tbl_cell_play);
        tbl_row.append(tbl_cell_no);
        tbl_row.append(tbl_cell_name);
        tbl_row.append(tbl_cell_duration);
        tbl_row.append(tbl_cell_artist);
        tbl_row.append(tbl_cell_country);
        tbl_row.append(tbl_cell_album);
        tbl_row.append(tbl_cell_year);
        tbl_row.append(tbl_cell_genre);
        play_button.click(function(event) {
            id = event.target.id
            console.log("get?id=" + id)
            updatePlayer(id)
        });
        table.append(tbl_row);
    });
    return false;
}

function fillRecentUploads() {
	$.getJSON("/recentuploads", function(json) {
        var table = $("#recentuploadstable");
        table.empty();
        headers(table, ["Artist", "Album", "Year", "Format"]);
        highlightOnClick(table);
        $.each(json, function() {
            var tbl_row = $("<tr>", {href: "#", class: "loadtracks", id: this.id});
            var tbl_cell_artist = $('<td>', {id: this.id, html: this.artist.name});
            var tbl_cell_album = $("<td>", {id: this.id, html: this.name});
            var tbl_cell_year = $("<td>", {id: this.id, html: this.year});
            var tbl_cell_format = $("<td>", {id: this.id, html: this.format});
            tbl_row.append(tbl_cell_artist);
            tbl_row.append(tbl_cell_album);
            tbl_row.append(tbl_cell_year);
            tbl_row.append(tbl_cell_format);
            var id = this.id;
            setClickHandlers(tbl_row, function() {
                $.getJSON("/tracks?albumid=" + id, function(json) {
                    console.log("/tracks?albumid=" + id);
                    fillTracks(json);
                });
            }, function() {
                alert("Double click");
            });
            table.append(tbl_row);
        });
        return false;
    });
}

function searchArtists(searchtext) {
    fillArtists("/searchartists?text=" + searchtext);
}

function playNext() {
    var nextID = 0;
    if (repeat) {
        nextID = tracks.indexOf(parseInt(currentId));
    } else if (shuffle) {
        nextID = randomInt(tracks.length);
    } else {
        nextID = tracks.indexOf(parseInt(currentId)) + 1;
        if (tracks[nextID] === undefined) nextID = 0;
    }
    console.log(nextID);
    var nextrow = $('#trackstable > tbody > tr > td> a[id=' + tracks[nextID] + ']');
    console.log(nextrow);
    nextrow.click();
}

function headers(table, headers) {
    var tbl_header_row = $("<tr>", {});
    $.each(headers, function() {
        var tbl_header_cell = $("<th>", {html: this});
        tbl_header_row.append(tbl_header_cell);
    });
    table.append(tbl_header_row);
}

function highlightOnClick(table) {
    table.on("click", "tbody tr", function(event) {
        $(this).addClass("success").siblings().removeClass("success");
    });
}

function setClickHandlers(row, singleClick, doubleClick) {
    row.on("click", function(e){
        clicks++;
        if(clicks === 1) {
            timer = setTimeout(function() {
                singleClick();
                clicks = 0;
            }, DELAY);
        } else {
            e.preventDefault();
            clearTimeout(timer);
            doubleClick();
            clicks = 0;
        }
    }).on("dblclick", function(e) {
        e.preventDefault();
    }).disableTextSelect();
}

function withLeadingZeros(num, size) {
	var s = num + "";
	while (s.length < size) s = "0" + s;
	return s;
}

function randomInt(maxValue) {
    return Math.floor(Math.random() * maxValue);
}