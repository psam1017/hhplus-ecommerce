// noinspection NpmUsedModulesInstalled,JSUnusedGlobalSymbols

import http from 'k6/http';

export const options = {
    vus: 30,
    duration: '90s',
}
export default function () {

    let url = 'http://localhost:8080/api/items/top';
    let params = {
        headers: {
            'Content-Type': 'application/json; charset=utf-8',
        },
    };
    http.get(url, params);
}
