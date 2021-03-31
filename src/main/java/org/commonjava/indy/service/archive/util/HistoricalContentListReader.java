/**
 * Copyright (C) 2011-2021 Red Hat, Inc. (https://github.com/Commonjava/indy)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.service.archive.util;

import org.commonjava.indy.service.archive.config.PreSeedConfig;
import org.commonjava.indy.service.archive.model.dto.HistoricalContentDTO;
import org.commonjava.indy.service.archive.model.dto.HistoricalEntryDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class HistoricalContentListReader
{
    private final String CONTENT_REST_BASE_PATH = "/api/content";

    @Inject
    PreSeedConfig preSeedConfig;

    public Map<String, String> readPaths( HistoricalContentDTO content  )
    {
        Map<String, String> pathMap = new HashMap<>();
        HistoricalEntryDTO[] downloads = content.getDownloads();

        if ( downloads != null )
        {
            for ( HistoricalEntryDTO download : downloads )
            {
                String path = download.getPath();
                if ( path.contains( "maven-metadata.xml" ) )
                {
                    // sidecar will get the maven-metadata.xml from main indy server
                    continue;
                }
                // ensure every entry has an available localUrl
                buildDownloadUrl( download );

                // local url would be preferred to download artifact
                String url = download.getLocalUrl();
                if ( url == null )
                {
                    url = download.getOriginUrl();
                }
                if ( url != null )
                {
                    pathMap.put( url, download.getFileStorePath() );
                }
            }
        }
        return pathMap;
    }

    private String buildDownloadUrl ( HistoricalEntryDTO download )
    {
        String baseUrl = preSeedConfig.indyServer.orElse( null );
        if ( baseUrl == null )
        {
            return null;
        }
        String downloadUrl = String.format( "%s%s%s%s", baseUrl, CONTENT_REST_BASE_PATH, download.getStorePath(), download.getPath() );
        download.setLocalUrl( downloadUrl );
        return downloadUrl;
    }
}
