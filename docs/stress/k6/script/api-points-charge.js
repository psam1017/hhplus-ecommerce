// noinspection NpmUsedModulesInstalled,JSUnusedGlobalSymbols

import http from 'k6/http';

export const options = {
    vus: 10,
    iterations: 10,
}
export default function () {

    let url = 'http://localhost:8080/api/users/' + 1 + '/points/charge';
    let payload = JSON.stringify({
        chargeAmount: 100,
    });
    let params = {
        headers: {
            'Content-Type': 'application/json; charset=utf-8',
        },
    };
    let post = http.post(url, payload, params);
    console.log(post.status);
}
