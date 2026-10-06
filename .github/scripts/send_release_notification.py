import json
import os
import sys
import time
import urllib.error
import urllib.request

import google.auth
from google.auth.transport.requests import Request


SCOPES = ["https://www.googleapis.com/auth/firebase.messaging"]
RETRYABLE_STATUS_CODES = {429, 500, 502, 503, 504}


def main() -> None:
    project_id = os.environ["FCM_PROJECT_ID"]
    credentials_info = json.loads(os.environ["FCM_SERVICE_ACCOUNT_JSON"])
    credentials, _ = google.auth.load_credentials_from_dict(credentials_info, scopes=SCOPES)
    credentials.refresh(Request())

    payload = {
        "message": {
            "topic": "production_app_updates",
            "data": {
                "schema_version": "1",
                "type": "app_update",
                "flavor": "production",
                "release_id": os.environ["GITHUB_REF_NAME"],
                "version_name": os.environ["RELEASE_VERSION_NAME"],
                "version_code": os.environ["RELEASE_VERSION_CODE"],
                "expires_at": os.environ["RELEASE_EXPIRES_AT"],
            },
            "android": {
                "priority": "HIGH",
                "ttl": "604800s",
                "collapse_key": "app_update_available",
            },
        }
    }
    body = json.dumps(payload).encode("utf-8")
    url = f"https://fcm.googleapis.com/v1/projects/{project_id}/messages:send"

    for attempt in range(4):
        request = urllib.request.Request(
            url,
            data=body,
            headers={
                "Authorization": f"Bearer {credentials.token}",
                "Content-Type": "application/json; UTF-8",
            },
            method="POST",
        )
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                result = json.load(response)
                print(f"FCM accepted release {payload['message']['data']['release_id']}: {result.get('name', 'accepted')}")
                return
        except urllib.error.HTTPError as error:
            response_body = error.read().decode("utf-8", errors="replace")[:2_000]
            if error.code not in RETRYABLE_STATUS_CODES or attempt == 3:
                raise RuntimeError(f"FCM send failed with HTTP {error.code}: {response_body}") from error
            time.sleep(2**attempt)

    sys.exit("FCM send failed after retries")


if __name__ == "__main__":
    main()
