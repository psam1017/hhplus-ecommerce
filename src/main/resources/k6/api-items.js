// noinspection NpmUsedModulesInstalled,JSUnusedGlobalSymbols

import http from 'k6/http';

export default function () {

    let url = 'http://localhost:8080/api/items';
    let params = {
        headers: {
            'Content-Type': 'application/json; charset=utf-8',
        },
    };
    http.get(url, params);
}
