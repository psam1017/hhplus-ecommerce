// noinspection NpmUsedModulesInstalled,JSUnusedGlobalSymbols,JSUnresolvedReference

import http from 'k6/http';

export const options = {
    vus: 500,
    iterations: 500,
}

export default function () {

    const userId = (__VU - 1) + (__ITER + 1);
    const url = `http://localhost:8080/api/users/${userId}/orders`;

    let payload = JSON.stringify({
        items: [
            {
                itemId: 1,
                amount: 1,
            },
        ],
    });
    let params = {
        headers: {
            'Content-Type': 'application/json; charset=utf-8',
        },
    };
    http.post(url, payload, params);
}
