import os
from elasticsearch import Elasticsearch
import json
import uuid

cloud_id = os.environ.get('CLOUD_ID')
es_user = os.environ.get('ES_USER')
es_pw = os.environ.get('ES_PW')

if cloud_id is not None and es_user is not None and es_pw is not None:
    elastic = Elasticsearch(
        cloud_id=cloud_id,
        http_auth=(es_user, es_pw)
    )

    staff_data = json.load(open("staff_data.json"))

    if elastic.indices.exists('expertise-search'):
        elastic.indices.delete('expertise-search')

    elastic.indices.create('expertise-search')

    for staff_member in staff_data:
        response = elastic.index(
            index='expertise-search',
            id=uuid.uuid4(),
            body=staff_member
        )