<?php

$foo = <<<EOT
{$bar[<<<'EOT'
b<caret>az
EOT
]}
EOT;
